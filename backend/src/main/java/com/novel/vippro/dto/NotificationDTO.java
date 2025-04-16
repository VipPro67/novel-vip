package com.novel.vippro.dto;

import com.novel.vippro.models.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private NotificationType type;
}