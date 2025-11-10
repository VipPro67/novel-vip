package com.novel.vippro.Messaging;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.novel.vippro.Models.User;
import com.novel.vippro.Messaging.payload.EmailVerificationMessage;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Services.EmailService;

@Component
public class EmailVerificationJobHandler {

	private static final Logger logger = LoggerFactory.getLogger(EmailVerificationJobHandler.class);

	private final UserRepository userRepository;
	private final EmailService emailService;

	public EmailVerificationJobHandler(UserRepository userRepository, EmailService emailService) {
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	public void handle(EmailVerificationMessage message) {
		if (message == null || message.getUserId() == null) {
			logger.warn("Received invalid email verification job payload: {}", message);
			return;
		}

		Optional<User> userOpt = userRepository.findById(message.getUserId());
		if (userOpt.isEmpty()) {
			logger.warn("Skipping email verification job. User {} not found", message.getUserId());
			return;
		}

		User user = userOpt.get();
		if (Boolean.TRUE.equals(user.getEmailVerified())) {
			logger.info("User {} already verified. Skipping email job.", user.getId());
			return;
		}

		if (user.getEmailVerificationToken() == null || user.getEmailVerificationToken().isBlank()) {
			logger.warn("User {} has no verification token. Skipping email job.", user.getId());
			return;
		}

		Instant expiresAt = user.getEmailVerificationExpiresAt();
		if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
			logger.info("Verification token for user {} already expired. Skipping email job.", user.getId());
			return;
		}

		Duration validity = Duration.ofSeconds(Math.max(message.getValidForSeconds(), 1L));
		try {
			emailService.sendEmailVerification(user, validity);
			user.setEmailVerificationSentAt(Instant.now());
			userRepository.save(user);
			logger.info("Verification email dispatched for user {}", user.getEmail());
		} catch (RuntimeException ex) {
			logger.error("Failed to send verification email for user {}", user.getEmail(), ex);
			throw ex;
		}
	}
}
