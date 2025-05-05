package com.novel.vippro.DTO.Notification;

import lombok.Data;
import java.util.UUID;

@Data
public class NotificationPreferencesDTO {
    private UUID userId;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean inAppNotifications;
    private boolean bookUpdates;
    private boolean chapterUpdates;
    private boolean comments;
    private boolean likes;
    private boolean follows;
    private boolean messages;
}