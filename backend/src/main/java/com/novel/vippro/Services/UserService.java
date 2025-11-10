package com.novel.vippro.Services;

import com.novel.vippro.DTO.Auth.ChangePasswordDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.DTO.User.UserSearchDTO;
import com.novel.vippro.DTO.User.UserUpdateDTO;
import com.novel.vippro.Exception.BadRequestException;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.ERole;
import com.novel.vippro.Models.Role;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.RoleRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Mapper mapper;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return new PageResponse<>(userPage.map(mapper::UsertoUserDTO));
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "#email")
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapper.UsertoUserDTO(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#updateDTO.id")
    public UserDTO updateUserProfile(UserUpdateDTO updateDTO) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("User", "id", null);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update full name if provided
        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isEmpty()) {
            user.setFullName(updateDTO.getFullName());
        }

        // Update avatar if provided
        if (updateDTO.getAvatar() != null && !updateDTO.getAvatar().isEmpty()) {
            user.setAvatar(updateDTO.getAvatar());
        }

        // ...existing code...

        User updatedUser = userRepository.save(user);
        return mapper.UsertoUserDTO(updatedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String oldPassword = changePasswordDTO.getOldPassword();
        String newPassword = changePasswordDTO.getNewPassword();
        if (!changePasswordDTO.isPasswordMatching()) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("User", "id", null);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public PageResponse<UserDTO> searchUsers(UserSearchDTO searchDTO) {
        // Create sort object
        Sort sort = Sort.by(
                searchDTO.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                searchDTO.getSortBy());

        // Create pageable object
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        // Use the custom search method if search parameters are provided
        Page<User> userPage = userRepository.searchUsers(searchDTO.getUsername(), searchDTO.getEmail(),
                searchDTO.getRole(), pageable);
        return new PageResponse<>(userPage.map(mapper::UsertoUserDTO));

    }

    @Transactional
    public UserDTO updateUserRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Role> roles = new ArrayList<>();

        if (roleNames == null || roleNames.isEmpty()) {
            // If no roles provided, set to USER
            Role userRole = roleRepository.findByName(ERole.USER)
                    .orElseThrow(() -> new BadRequestException("Role not found"));
            roles.add(userRole);
        } else {
            roleNames.forEach(role -> {
                Role foundRole = roleRepository.findByName(ERole.valueOf(role))
                        .orElseThrow(() -> new BadRequestException("Role not found: " + role));
                roles.add(foundRole);
            });
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return mapper.UsertoUserDTO(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        userRepository.deleteById(userId);
    }
}
