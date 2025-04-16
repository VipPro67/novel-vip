package com.novel.vippro.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SubscriptionPlanDTO {
    private UUID id;
    private String planId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String billingPeriod;
    private List<String> features;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}