package com.novel.vippro.DTO.Notification;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.Models.NotificationType;

@Data
public class CreateNotificationDTO {
    private UUID userId;
    private String title;
    private String message;
    private NotificationType type;
    private String referenceId; 
}