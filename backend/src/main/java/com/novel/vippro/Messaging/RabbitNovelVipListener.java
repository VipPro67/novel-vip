package com.novel.vippro.Messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Services.NovelVipPublisher;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitNovelVipListener {

    private final NovelVipPublisher publisher;

    public RabbitNovelVipListener(NovelVipPublisher publisher) {
        this.publisher = publisher;
    }

    @RabbitListener(queues = MessageQueues.NOTIFICATION)
    public void handleNotification(NotificationDTO notification) {
        publisher.publishNotification(notification);
    }

    @RabbitListener(queues = MessageQueues.COMMENT)
    public void handleComment(CommentDTO comment) {
        publisher.publishComment(comment);
    }
}
