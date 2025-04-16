package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String planId;
    private String planName;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}