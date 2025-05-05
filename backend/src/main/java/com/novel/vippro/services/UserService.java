package com.novel.vippro.services;

import com.novel.vippro.dto.ChangePasswordDTO;
import com.novel.vippro.dto.UserDTO;
import com.novel.vippro.dto.UserSearchDTO;
import com.novel.vippro.dto.UserUpdateDTO;
import com.novel.vippro.exception.BadRequestException;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.ERole;
import com.novel.vippro.models.Role;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.RoleRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Cacheable(value = "users", key = "'all-' + #page + '-' + #size")
    public PageResponse<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return new PageResponse<>(userPage.map(this::convertToDTO));
    }

    public User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
            return null;
        }
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));
    }

    public UUID getCurrentUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
            return null;
        }
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
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
    public UserDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return convertToDTO(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#updateDTO.id")
    public UserDTO updateUserProfile(UserUpdateDTO updateDTO) {
        UUID userId = getCurrentUserId();
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

        // Update email if provided and not already taken
        // if (updateDTO.getEmail() != null && !updateDTO.getEmail().isEmpty()) {
        // if (!user.getEmail().equals(updateDTO.getEmail()) &&
        // userRepository.existsByEmail(updateDTO.getEmail())) {
        // throw new BadRequestException("Email is already in use");
        // }
        // user.setEmail(updateDTO.getEmail());
        // }

        // Update password if provided
        // if (updateDTO.getNewPassword() != null &&
        // !updateDTO.getNewPassword().isEmpty()) {
        // if (updateDTO.getCurrentPassword() == null ||
        // !passwordEncoder.matches(updateDTO.getCurrentPassword(), user.getPassword()))
        // {
        // throw new BadRequestException("Current password is incorrect");
        // }
        // user.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
        // }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String oldPassword = changePasswordDTO.getOldPassword();
        String newPassword = changePasswordDTO.getNewPassword();
        if (!changePasswordDTO.isPasswordMatching()) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        UUID userId = getCurrentUserId();
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
        return new PageResponse<>(userPage.map(this::convertToDTO));

    }

    @Transactional
    public UserDTO updateUserRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();

        if (roleNames == null || roleNames.isEmpty()) {
            // If no roles provided, set to USER
            Role userRole = roleRepository.findByName(ERole.USER)
                    .orElseThrow(() -> new BadRequestException("Role not found"));
            roles.add(userRole);
        } else {
            roleNames.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                                .orElseThrow(() -> new BadRequestException("Role not found"));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.MODERATOR)
                                .orElseThrow(() -> new BadRequestException("Role not found"));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.USER)
                                .orElseThrow(() -> new BadRequestException("Role not found"));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        userRepository.deleteById(userId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName());

        // Convert roles to strings
        Set<String> roleStrings = user.getRoles().stream()
                .map(role -> role.getName().name().toLowerCase())
                .collect(Collectors.toSet());

        dto.setRoles(roleStrings);
        return dto;
    }
}