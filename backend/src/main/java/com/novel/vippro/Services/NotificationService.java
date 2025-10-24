package com.novel.vippro.Services;

import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Notification;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Repository.NotificationRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private MessagePublisher messagePublisher;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Transactional
    public NotificationDTO createNotification(CreateNotificationDTO notificationDTO) {
        User user = userRepository.findById(notificationDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setReference(notificationDTO.getReference());
        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = mapper.NotificationtoDTO(saved);
        messagePublisher.publishNotification(dto);
        return dto;
    }

    public PageResponse<NotificationDTO> getUserNotifications(Pageable pageable) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        PageResponse<NotificationDTO> response = new PageResponse<>(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
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
        UUID userId = UserDetailsImpl.getCurrentUserId();
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
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
        UUID userId = UserDetailsImpl.getCurrentUserId();
        return notificationRepository.countByUserIdAndReadFalse(userId);
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
        UUID userId = UserDetailsImpl.getCurrentUserId();
        notificationRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteAllUserNotifications(UUID userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
