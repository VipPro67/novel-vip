package com.novel.vippro.services;

import com.novel.vippro.dto.NotificationDTO;
import com.novel.vippro.dto.NotificationPreferencesDTO;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.Notification;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.NotificationRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.checkerframework.checker.units.qual.m;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private Mapper mapper;

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
        return mapper.NotificationtoDTO(savedNotification);
    }

    public PageResponse<NotificationDTO> getUserNotifications(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserId();
        PageResponse<NotificationDTO> response = new PageResponse<>(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                        .map(mapper::NotificationtoDTO));
        return response;
    }

    public PageResponse<NotificationDTO> getUserNotifications(UUID userId, Pageable pageable) {
        return new PageResponse<>(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::NotificationtoDTO));

    }

    @Transactional
    public NotificationDTO markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return mapper.NotificationtoDTO(savedNotification);
    }

    @Transactional
    public void markAllAsRead() {
        UUID currentUserId = userService.getCurrentUserId();
        notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    public long getUnreadNotificationsCount() {
        UUID currentUserId = userService.getCurrentUserId();
        return notificationRepository.countByUserIdAndReadFalse(currentUserId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications() {
        UUID currentUserId = userService.getCurrentUserId();
        notificationRepository.deleteByUserId(currentUserId);
    }

    @Transactional
    public void deleteAllUserNotifications(UUID userId) {
        notificationRepository.deleteByUserId(userId);
    }

    @Transactional
    public NotificationPreferencesDTO updatePreferences(NotificationPreferencesDTO preferences) {
        // TODO: Implement notification preferences update logic
        return preferences;
    }
}