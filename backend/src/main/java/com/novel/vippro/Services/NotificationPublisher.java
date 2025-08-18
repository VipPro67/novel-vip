package com.novel.vippro.Services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.novel.vippro.Config.RabbitMQConfig;
import com.novel.vippro.DTO.Notification.NotificationDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
        private final SimpMessagingTemplate messagingTemplate;
        private final NotificationService notificationService;

        @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
        public void handleNotification(NotificationDTO notification) {
                NotificationDTO saved = notificationService.createNotification(notification);
                messagingTemplate.convertAndSend("/topic/user." + saved.getUserId(), saved);
        }

        @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
        public void handleCommentNotification(NotificationDTO notification) {
                NotificationDTO saved = notificationService.createNotification(notification);
                messagingTemplate.convertAndSend("/topic/user." + saved.getUserId(), saved);
        }
}

