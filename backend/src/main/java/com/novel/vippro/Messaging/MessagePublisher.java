package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;

/**
 * Broker-agnostic contract for publishing messages to the async queue.
 */
public interface MessagePublisher {

    void publishNotification(NotificationDTO notification);

    void publishComment(CommentDTO comment);
}
