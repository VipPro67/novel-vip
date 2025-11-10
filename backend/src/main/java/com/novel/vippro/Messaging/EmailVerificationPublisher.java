package com.novel.vippro.Messaging;

import java.time.Duration;
import java.util.UUID;

public interface EmailVerificationPublisher {
	void publishEmailVerification(UUID userId, Duration validFor);
}
