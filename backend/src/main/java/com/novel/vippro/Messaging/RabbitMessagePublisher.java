package com.novel.vippro.Messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMessagePublisher implements MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishNotification(NotificationDTO notification) {
        rabbitTemplate.convertAndSend(MessageQueues.NOTIFICATION, notification);
    }

    @Override
    public void publishComment(CommentDTO comment) {
        rabbitTemplate.convertAndSend(MessageQueues.COMMENT, comment);
    }
}
