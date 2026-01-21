package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RabbitShubaImportListener {

    private final ShubaImportProcessor processor;

    @RabbitListener(queues = MessageQueues.SHUBA_IMPORT)
    public void handle(ShubaImportMessage message) {
        log.info("Received Shuba import job {}", message.getJobId());
        processor.process(message);
    }
}
