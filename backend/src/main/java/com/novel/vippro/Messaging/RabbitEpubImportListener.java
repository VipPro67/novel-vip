package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.EpubImportMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitEpubImportListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitEpubImportListener.class);

    private final EpubImportProcessor processor;

    public RabbitEpubImportListener(EpubImportProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = MessageQueues.EPUB_UPLOAD)
    public void handle(EpubImportMessage message) {
        logger.info("Received EPUB import job {}", message.getJobId());
        processor.process(message);
    }
}
