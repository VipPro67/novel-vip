package com.novel.vippro.Controllers;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.DTO.Notification.NotificationPreferencesDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.NotificationService;

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
    public ControllerResponse<NotificationDTO> createNotification(@RequestBody CreateNotificationDTO notificationDTO) {
        return ControllerResponse.success(notificationService.createNotification(notificationDTO));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<PageResponse<NotificationDTO>> getUserNotifications(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ControllerResponse.success(notificationService.getUserNotifications(userId, pageable));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<NotificationDTO> markAsRead(@PathVariable UUID notificationId) {
        return ControllerResponse.success(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<Void> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ControllerResponse.success(
                "All notifications marked as read for user: " + userId, null);
    }

    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<Long> getUnreadCount(@PathVariable UUID userId) {
        return ControllerResponse.success(notificationService.getUnreadCount(userId));
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<Void> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return ControllerResponse.success(
                "Notification deleted successfully", null);
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ControllerResponse<Void> deleteAllUserNotifications(@PathVariable UUID userId) {
        notificationService.deleteAllUserNotifications(userId);
        return ControllerResponse.success(
                "All notifications deleted for user: " + userId, null);
    }

    @Operation(summary = "Get user notifications", description = "Get all notifications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ControllerResponse<PageResponse<NotificationDTO>> getUserNotifications(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NotificationDTO> notifications = notificationService.getUserNotifications(pageable);
        return ControllerResponse.success("Notifications retrieved successfully", notifications);
    }

    @Operation(summary = "Get unread notification count", description = "Get the count of unread notifications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/unread/count")
    public ControllerResponse<Long> getUnreadCount() {
        Long count = notificationService.getUnreadNotificationsCount();
        return ControllerResponse.success("Unread count retrieved successfully", count);
    }

    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications of the current user as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/read-all")
    public ControllerResponse<ControllerResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ControllerResponse.success(ControllerResponse.success("All notifications marked as read", null));
    }

    @Operation(summary = "Delete all notifications", description = "Delete all notifications of the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/all")
    public ControllerResponse<ControllerResponse<Void>> deleteAllNotifications() {
        notificationService.deleteAllNotifications();
        return ControllerResponse.success(ControllerResponse.success("All notifications deleted", null));
    }

}