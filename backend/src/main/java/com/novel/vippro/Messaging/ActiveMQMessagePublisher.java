package com.novel.vippro.Messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQMessagePublisher implements MessagePublisher {

    private final JmsTemplate jmsTemplate;

    public ActiveMQMessagePublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void publishNotification(NotificationDTO notification) {
        jmsTemplate.convertAndSend(MessageQueues.NOTIFICATION, notification);
    }

    @Override
    public void publishComment(CommentDTO comment) {
        jmsTemplate.convertAndSend(MessageQueues.COMMENT, comment);
    }
}
