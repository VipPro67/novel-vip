package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.ChapterAudioMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitChapterAudioListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitChapterAudioListener.class);

    private final ChapterAudioProcessor processor;

    public RabbitChapterAudioListener(ChapterAudioProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = MessageQueues.CHAPTER_AUDIO)
    public void handle(ChapterAudioMessage message) {
        logger.info("Received chapter audio job for chapter {}", message.getChapterId());
        processor.process(message);
    }
}
