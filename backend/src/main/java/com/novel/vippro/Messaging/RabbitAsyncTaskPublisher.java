package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitAsyncTaskPublisher implements AsyncTaskPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitAsyncTaskPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitAsyncTaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishEpubImport(EpubImportMessage message) {
        logger.info("Publishing EPUB import job {} to queue {}", message.getJobId(), MessageQueues.EPUB_UPLOAD);
        rabbitTemplate.convertAndSend(MessageQueues.EPUB_UPLOAD, message);
    }

    @Override
    public void publishChapterAudio(ChapterAudioMessage message) {
        logger.info("Publishing chapter audio job for chapter {} to queue {}", message.getChapterId(),
                MessageQueues.CHAPTER_AUDIO);
        rabbitTemplate.convertAndSend(MessageQueues.CHAPTER_AUDIO, message);
    }
}
