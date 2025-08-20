package com.novel.vippro.Services;

import com.novel.vippro.Config.RabbitMQConfig;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	// Listen to notifications
	@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
	public void handleNotification(NotificationDTO notification) {
		// Forward to frontend subscribers
		messagingTemplate.convertAndSend(
				"/topic/user." + notification.getUserId(),
				notification);
	}

	// Optional: listen to comment notifications separately
	@RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
	public void handleCommentNotification(NotificationDTO notification) {
		messagingTemplate.convertAndSend(
				"/topic/user." + notification.getUserId(),
				notification);
	}
}
