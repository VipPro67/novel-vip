package com.novel.vippro.DTO.Notification;

import lombok.Builder;
import java.util.UUID;

@Builder
public record NotificationPreferencesDTO(
    UUID userId,
    boolean emailNotifications,
    boolean pushNotifications,
    boolean inAppNotifications,
    boolean bookUpdates,
    boolean chapterUpdates,
    boolean comments,
    boolean likes,
    boolean follows,
    boolean messages
) {}