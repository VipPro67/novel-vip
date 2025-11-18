package com.novel.vippro.DTO.Notification;

import com.novel.vippro.Models.NotificationType;
import lombok.Builder;
import java.util.UUID;

@Builder
public record CreateNotificationDTO(
    UUID userId,
    String title,
    String message,
    NotificationType type,
    String reference
) {}