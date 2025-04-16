package com.novel.vippro.controllers;

import com.novel.vippro.dto.UserDTO;
import com.novel.vippro.dto.UserSearchDTO;
import com.novel.vippro.dto.UserUpdateDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.UserService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view users"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Page<UserDTO>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ControllerResponse.success("Users retrieved successfully", users));
    }

    @Operation(summary = "Get user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ControllerResponse<UserDTO>> getUserProfile() {
        UUID userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("User", "id", null);
        }
        UserDTO userDTO = userService.getUserProfile(userId);
        return ResponseEntity.ok(ControllerResponse.success("User profile retrieved successfully", userDTO));
    }

    @Operation(summary = "Update user profile", description = "Update the profile information of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/profile")
    public ResponseEntity<ControllerResponse<UserDTO>> updateUserProfile(
            @Parameter(description = "ID of the user to update") @RequestParam UUID userId,
            @Parameter(description = "Updated user information") @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO updatedUser = userService.updateUserProfile(userId, userUpdateDTO);
        return ResponseEntity.ok(ControllerResponse.success("User profile updated successfully", updatedUser));
    }

    @Operation(summary = "Search users", description = "Search for users based on various criteria. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to search users"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Page<UserDTO>>> searchUsers(
            @Parameter(description = "Search criteria") @ModelAttribute UserSearchDTO searchDTO) {
        Page<UserDTO> users = userService.searchUsers(searchDTO);
        return ResponseEntity.ok(ControllerResponse.success("Users retrieved successfully", users));
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<UserDTO>> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable UUID id) {
        UserDTO user = userService.getUserProfile(id);
        return ResponseEntity.ok(ControllerResponse.success("User retrieved successfully", user));
    }

    @Operation(summary = "Update user roles", description = "Update the roles assigned to a specific user. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "400", description = "Invalid role data")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<UserDTO>> updateUserRoles(
            @Parameter(description = "ID of the user to update") @PathVariable UUID id,
            @Parameter(description = "List of role names to assign") @RequestBody Set<String> roles) {
        UserDTO updatedUser = userService.updateUserRoles(id, roles);
        return ResponseEntity.ok(ControllerResponse.success("User roles updated successfully", updatedUser));
    }

    @Operation(summary = "Delete user", description = "Delete a specific user. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Void>> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ControllerResponse.success("User deleted successfully", null));
    }
}