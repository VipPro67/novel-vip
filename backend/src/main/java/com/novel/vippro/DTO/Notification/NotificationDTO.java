package com.novel.vippro.DTO.Notification;

import com.novel.vippro.Models.NotificationType;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record NotificationDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UUID userId,
    String title,
    String message,
    boolean read,
    NotificationType type,
    String reference
) {}