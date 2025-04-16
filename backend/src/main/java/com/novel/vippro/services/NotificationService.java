package com.novel.vippro.services;

import com.novel.vippro.dto.NotificationDTO;
import com.novel.vippro.dto.NotificationPreferencesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface NotificationService {
    NotificationDTO createNotification(NotificationDTO notificationDTO);

    Page<NotificationDTO> getUserNotifications(Pageable pageable);

    Page<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable);

    NotificationDTO markAsRead(UUID notificationId);

    void markAllAsRead();

    void markAllAsRead(UUID userId);

    long getUnreadNotificationsCount();

    long getUnreadCount(UUID userId);

    void deleteNotification(UUID notificationId);

    void deleteAllNotifications();

    void deleteAllUserNotifications(UUID userId);

    NotificationPreferencesDTO updatePreferences(NotificationPreferencesDTO preferences);
}