package com.novel.vippro.Messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQNovelVipListener {

    private final NovelVipPublisher publisher;

    public ActiveMQNovelVipListener(NovelVipPublisher publisher) {
        this.publisher = publisher;
    }

    @JmsListener(destination = MessageQueues.NOTIFICATION)
    public void handleNotification(NotificationDTO notification) {
        publisher.publishNotification(notification);
    }

    @JmsListener(destination = MessageQueues.COMMENT)
    public void handleComment(CommentDTO comment) {
        publisher.publishComment(comment);
    }
}
