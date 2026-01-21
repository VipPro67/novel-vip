package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import com.novel.vippro.Messaging.payload.EmailVerificationMessage;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQMessagePublisher implements MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMQMessagePublisher.class);

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

    @Override
    public void publishEpubImport(EpubImportMessage message) {
        logger.info("Publishing EPUB import job {} to queue {}", message.getJobId(), MessageQueues.EPUB_UPLOAD);
        jmsTemplate.convertAndSend(MessageQueues.EPUB_UPLOAD, message);
    }

    @Override
    public void publishChapterAudio(ChapterAudioMessage message) {
        logger.info("Publishing chapter audio job for chapter {} to queue {}", message.getChapterId(),
                MessageQueues.CHAPTER_AUDIO);
        jmsTemplate.convertAndSend(MessageQueues.CHAPTER_AUDIO, message);
    }

    @Override
    public void publishEmailVerification(UUID userId, Duration validFor) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to publish verification email job");
        }
        long seconds = Math.max(validFor != null ? validFor.getSeconds() : 0, 1L);
        EmailVerificationMessage payload = new EmailVerificationMessage(userId, seconds,
                Instant.now().getEpochSecond());
        jmsTemplate.convertAndSend(MessageQueues.EMAIL_VERIFICATION, payload);
        logger.debug("Queued email verification job via ActiveMQ for user {}", userId);
    }

    @Override
    public void publishShubaImport(ShubaImportMessage message) {
        logger.info("Publishing Shuba import job {} to queue {}", message.getJobId(), MessageQueues.SHUBA_IMPORT);
        jmsTemplate.convertAndSend(MessageQueues.SHUBA_IMPORT, message);
    }
}
