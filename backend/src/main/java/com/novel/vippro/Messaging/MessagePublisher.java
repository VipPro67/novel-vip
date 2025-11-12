package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import java.time.Duration;
import java.util.UUID;

/**
 * Broker-agnostic contract for publishing messages to the async queue.
 */
public interface MessagePublisher {

    void publishNotification(NotificationDTO notification);

    void publishComment(CommentDTO comment);

    void publishEpubImport(EpubImportMessage message);

    void publishChapterAudio(ChapterAudioMessage message);

    void publishEmailVerification(UUID userId, Duration validFor);
}
