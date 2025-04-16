package com.novel.vippro.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String paymentGatewayId;
    private UUID subscriptionId;
    private String description;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}