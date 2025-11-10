package com.novel.vippro.Messaging;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.novel.vippro.Messaging.payload.EmailVerificationMessage;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitEmailVerificationPublisher implements EmailVerificationPublisher {

	private static final Logger logger = LoggerFactory.getLogger(RabbitEmailVerificationPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public RabbitEmailVerificationPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void publishEmailVerification(UUID userId, Duration validFor) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID is required to publish verification email job");
		}
		long seconds = Math.max(validFor != null ? validFor.getSeconds() : 0, 1L);
		EmailVerificationMessage payload = new EmailVerificationMessage(userId, seconds,
				Instant.now().getEpochSecond());
		rabbitTemplate.convertAndSend(MessageQueues.EMAIL_VERIFICATION, payload);
		logger.debug("Queued email verification job for user {}", userId);
	}
}
