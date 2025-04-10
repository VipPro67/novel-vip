package com.novel.vippro.controllers;

import com.novel.vippro.dto.UserDTO;
import com.novel.vippro.dto.UserSearchDTO;
import com.novel.vippro.dto.UserUpdateDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.payload.response.ApiResponse;
import com.novel.vippro.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(@RequestParam UUID userId) {
        UserDTO userDTO = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userDTO));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @RequestParam UUID userId,
            @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUserProfile(userId, userUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", updatedUser));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @ModelAttribute UserSearchDTO searchDTO) {
        Page<UserDTO> users = userService.searchUsers(searchDTO);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody Set<String> roles) {
        UserDTO updatedUser = userService.updateUserRoles(id, roles);
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}