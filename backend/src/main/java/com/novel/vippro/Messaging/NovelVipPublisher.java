package com.novel.vippro.Messaging;

import com.novel.vippro.Controllers.NotificationStreamController;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NovelVipPublisher {

	private final SimpMessagingTemplate messagingTemplate;
	private final NotificationStreamController notificationStreamController;

    public void publishNotification(NotificationDTO notification) {
        // Send via SSE
        notificationStreamController.sendNotificationToUser(notification.userId(), notification);
        
        // Keep WebSocket for backward compatibility (can be removed later)
        messagingTemplate.convertAndSend(
                "/topic/user." + notification.userId(),
                notification);
    }

    public void publishComment(CommentDTO comment) {
        StringBuilder destination = new StringBuilder("/topic/novel.").append(comment.novelId());
        if (comment.chapterId() != null) {
            destination.append(".chapter.").append(comment.chapterId());
        }
        messagingTemplate.convertAndSend(destination.toString(), comment);
    }
}
