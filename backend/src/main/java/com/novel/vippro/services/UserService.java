package com.novel.vippro.services;

import com.novel.vippro.dto.UserDTO;
import com.novel.vippro.dto.UserSearchDTO;
import com.novel.vippro.dto.UserUpdateDTO;
import com.novel.vippro.exception.BadRequestException;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.models.ERole;
import com.novel.vippro.models.Role;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.RoleRepository;
import com.novel.vippro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUserProfile(UUID userId, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update full name if provided
        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isEmpty()) {
            user.setFullName(updateDTO.getFullName());
        }

        // Update email if provided and not already taken
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().isEmpty()) {
            if (!user.getEmail().equals(updateDTO.getEmail()) &&
                    userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // Update password if provided
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty()) {
            if (updateDTO.getCurrentPassword() == null ||
                    !passwordEncoder.matches(updateDTO.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public Page<UserDTO> searchUsers(UserSearchDTO searchDTO) {
        // Create sort object
        Sort sort = Sort.by(
                searchDTO.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                searchDTO.getSortBy());

        // Create pageable object
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        // Use the custom search method if search parameters are provided
        if (searchDTO.getUsername() != null || searchDTO.getEmail() != null) {
            Page<User> userPage = userRepository.searchUsers(
                    searchDTO.getUsername(),
                    searchDTO.getEmail(),
                    null, // fullName search not implemented in DTO yet
                    pageable);

            // Filter by role if specified
            if (searchDTO.getRole() != null && !searchDTO.getRole().isEmpty()) {
                List<User> filteredUsers = userPage.getContent().stream()
                        .filter(user -> user.getRoles().stream()
                                .anyMatch(role -> role.getName().name().toLowerCase()
                                        .equals(searchDTO.getRole().toLowerCase())))
                        .collect(Collectors.toList());

                return new PageImpl<>(
                        filteredUsers.stream().map(this::convertToDTO).collect(Collectors.toList()),
                        pageable,
                        filteredUsers.size());
            }

            return userPage.map(this::convertToDTO);
        }

        // If no search parameters, get all users with pagination
        Page<User> userPage = userRepository.findAll(pageable);

        // Filter by role if specified
        if (searchDTO.getRole() != null && !searchDTO.getRole().isEmpty()) {
            List<User> filteredUsers = userPage.getContent().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName().name().toLowerCase()
                                    .equals(searchDTO.getRole().toLowerCase())))
                    .collect(Collectors.toList());

            return new PageImpl<>(
                    filteredUsers.stream().map(this::convertToDTO).collect(Collectors.toList()),
                    pageable,
                    filteredUsers.size());
        }

        // Convert to DTOs
        return userPage.map(this::convertToDTO);
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