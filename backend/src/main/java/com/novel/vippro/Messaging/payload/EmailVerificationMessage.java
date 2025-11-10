package com.novel.vippro.Messaging.payload;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID userId;
	private long validForSeconds;
	private long requestedAtEpochSeconds;
}
