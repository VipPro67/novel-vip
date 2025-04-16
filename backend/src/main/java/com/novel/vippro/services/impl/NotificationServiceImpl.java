package com.novel.vippro.services.impl;

import com.novel.vippro.dto.NotificationDTO;
import com.novel.vippro.dto.NotificationPreferencesDTO;
import com.novel.vippro.models.Notification;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.NotificationRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        User user = userRepository.findById(notificationDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());

        Notification savedNotification = notificationRepository.save(notification);
        return convertToDTO(savedNotification);
    }

    @Override
    public Page<NotificationDTO> getUserNotifications(Pageable pageable) {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setRead(true);
        return convertToDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder
        notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    public long getUnreadNotificationsCount() {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder
        return notificationRepository.countByUserIdAndReadFalse(currentUserId);
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void deleteAllNotifications() {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder
        notificationRepository.deleteByUserId(currentUserId);
    }

    @Override
    @Transactional
    public void deleteAllUserNotifications(UUID userId) {
        notificationRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public NotificationPreferencesDTO updatePreferences(NotificationPreferencesDTO preferences) {
        // TODO: Implement notification preferences update logic
        return preferences;
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setType(notification.getType());
        return dto;
    }
}