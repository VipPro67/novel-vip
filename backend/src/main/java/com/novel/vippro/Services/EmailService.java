package com.novel.vippro.Services;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import com.novel.vippro.Models.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String senderEmail;
	private final String frontendBaseUrl;

	public EmailService(JavaMailSender mailSender,
			@Value("${app.mail.from:no-reply@novel-vip.com}") String senderEmail,
			@Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl) {
		this.mailSender = mailSender;
		this.senderEmail = senderEmail;
		this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
				? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
				: frontendBaseUrl;
	}

	public void sendEmailVerification(@NonNull User user, Duration validity) {
		Assert.notNull(user.getEmail(), "User email must not be null");
		Assert.hasText(user.getEmailVerificationToken(), "User verification token must be set before sending email");

		String verificationUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
				.path("/verify-email")
				.queryParam("token", user.getEmailVerificationToken())
				.build()
				.toUriString();

		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
			helper.setFrom(senderEmail);
			helper.setTo(user.getEmail());
			helper.setSubject("Verify your Novel VIP email");
			helper.setText(buildVerificationTemplate(user.getUsername(), verificationUrl, validity), true);

			mailSender.send(mimeMessage);
			logger.info("Verification email sent to {}", user.getEmail());
		} catch (MessagingException | MailException e) {
			logger.error("Failed to send verification email to {}", user.getEmail(), e);
			throw new MailSendException("Failed to send verification email", e);
		}
	}

	private String buildVerificationTemplate(String username, String verificationUrl, Duration validity) {
		long hours = validity.toHours();
		String timeframe = hours >= 24
				? String.format("%d day%s", hours / 24, hours / 24 > 1 ? "s" : "")
				: String.format("%d hour%s", Math.max(hours, 1), Math.max(hours, 1) > 1 ? "s" : "");

		return """
				<html>
				  <body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #111827;\">
				    <h2>Verify your email</h2>
				    <p>Hi %s,</p>
				    <p>Welcome to Novel VIP! Please confirm your email address to finish creating your account.</p>
				    <p style=\"margin: 24px 0;\">
				      <a href=\"%s\" style=\"background-color: #7c3aed; color: #ffffff; padding: 12px 20px; text-decoration: none; border-radius: 6px;\">Verify email</a>
				    </p>
				    <p>For your security, this link will expire in %s. If it expires, you can request a new one from the login screen.</p>
				    <p>If you didn't create this account, please ignore this email.</p>
				    <p style=\"margin-top: 40px; color: #6b7280; font-size: 12px;\">© %d Novel VIP</p>
				  </body>
				</html>
			""".formatted(username != null ? username : "there", verificationUrl, timeframe,
				java.time.Year.now().getValue());
	}
}
