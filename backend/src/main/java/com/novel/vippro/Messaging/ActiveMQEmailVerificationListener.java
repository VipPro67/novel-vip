package com.novel.vippro.Messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.novel.vippro.Messaging.payload.EmailVerificationMessage;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQEmailVerificationListener {

	private final EmailVerificationJobHandler handler;

	public ActiveMQEmailVerificationListener(EmailVerificationJobHandler handler) {
		this.handler = handler;
	}

	@JmsListener(destination = MessageQueues.EMAIL_VERIFICATION)
	public void handle(EmailVerificationMessage message) {
		handler.handle(message);
	}
}
