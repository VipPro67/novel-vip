package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NovelVipPublisher {

	private final SimpMessagingTemplate messagingTemplate;

    public void publishNotification(NotificationDTO notification) {
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
