package com.novel.vippro.DTO.Notification;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.NotificationType;

@Data
public class NotificationDTO extends BaseDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private boolean read;
    private NotificationType type;
    private String reference; 
}