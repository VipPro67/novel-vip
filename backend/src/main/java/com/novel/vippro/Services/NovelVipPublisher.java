package com.novel.vippro.Services;

import com.novel.vippro.Config.RabbitMQConfig;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NovelVipPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	// Listen to notifications
	@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
	public void handleNotification(NotificationDTO notification) {
		// Forward to frontend subscribers => load count and content at noti popup
		messagingTemplate.convertAndSend(
				"/topic/user." + notification.getUserId(),
				notification);
	} 

    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleNotification(CommentDTO comment) {
		// Forward to frontend subscribers => load again comment section at novel
		messagingTemplate.convertAndSend(
				"/topic/novel." + comment.getNovelId() + comment.getChapterId()!= null ? "chapter." + comment.getChapterId() : "",
				comment);
	}
}
