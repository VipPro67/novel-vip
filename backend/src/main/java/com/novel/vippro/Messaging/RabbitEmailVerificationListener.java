package com.novel.vippro.Messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.novel.vippro.Messaging.payload.EmailVerificationMessage;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitEmailVerificationListener {

	private final EmailVerificationJobHandler handler;

	public RabbitEmailVerificationListener(EmailVerificationJobHandler handler) {
		this.handler = handler;
	}

	@RabbitListener(queues = MessageQueues.EMAIL_VERIFICATION)
	public void handle(EmailVerificationMessage message) {
		handler.handle(message);
	}
}
