package com.novel.vippro.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.dto.NotificationDTO;
import com.novel.vippro.dto.NotificationPreferencesDTO;
import com.novel.vippro.services.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User notification management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {

        private final NotificationService notificationService;

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationDTO notificationDTO) {
                return ResponseEntity.ok(notificationService.createNotification(notificationDTO));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
                        @PathVariable UUID userId,
                        Pageable pageable) {
                return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
        }

        @PutMapping("/{notificationId}/read")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<NotificationDTO> markAsRead(@PathVariable UUID notificationId) {
                return ResponseEntity.ok(notificationService.markAsRead(notificationId));
        }

        @PutMapping("/user/{userId}/read-all")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
                notificationService.markAllAsRead(userId);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/user/{userId}/unread-count")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<Long> getUnreadCount(@PathVariable UUID userId) {
                return ResponseEntity.ok(notificationService.getUnreadCount(userId));
        }

        @DeleteMapping("/{notificationId}")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
                notificationService.deleteNotification(notificationId);
                return ResponseEntity.ok().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteAllUserNotifications(@PathVariable UUID userId) {
                notificationService.deleteAllUserNotifications(userId);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Get user notifications", description = "Get all notifications for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping
        public ResponseEntity<ControllerResponse<Page<NotificationDTO>>> getUserNotifications(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<NotificationDTO> notifications = notificationService.getUserNotifications(pageable);
                return ResponseEntity
                                .ok(ControllerResponse.success("Notifications retrieved successfully", notifications));
        }

        @Operation(summary = "Get unread notification count", description = "Get the count of unread notifications for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/unread/count")
        public ResponseEntity<ControllerResponse<Long>> getUnreadCount() {
                Long count = notificationService.getUnreadNotificationsCount();
                return ResponseEntity.ok(ControllerResponse.success("Unread count retrieved successfully", count));
        }

        @Operation(summary = "Mark all notifications as read", description = "Mark all notifications of the current user as read")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PutMapping("/read-all")
        public ResponseEntity<ControllerResponse<Void>> markAllAsRead() {
                notificationService.markAllAsRead();
                return ResponseEntity.ok(ControllerResponse.success("All notifications marked as read", null));
        }

        @Operation(summary = "Delete all notifications", description = "Delete all notifications of the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All notifications deleted"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @DeleteMapping("/all")
        public ResponseEntity<ControllerResponse<Void>> deleteAllNotifications() {
                notificationService.deleteAllNotifications();
                return ResponseEntity.ok(ControllerResponse.success("All notifications deleted", null));
        }

        @Operation(summary = "Update notification preferences", description = "Update notification preferences for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid preferences data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PutMapping("/preferences")
        public ResponseEntity<ControllerResponse<NotificationPreferencesDTO>> updatePreferences(
                        @Parameter(description = "Updated preferences", required = true) @Valid @RequestBody NotificationPreferencesDTO preferences) {
                NotificationPreferencesDTO updatedPreferences = notificationService.updatePreferences(preferences);
                return ResponseEntity
                                .ok(ControllerResponse.success("Notification preferences updated", updatedPreferences));
        }
}