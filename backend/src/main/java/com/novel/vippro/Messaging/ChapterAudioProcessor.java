package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.NotificationType;
import com.novel.vippro.Models.SystemJob;
import com.novel.vippro.Services.ChapterService;
import com.novel.vippro.Services.NotificationService;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class ChapterAudioProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ChapterAudioProcessor.class);

    private final ChapterService chapterService;
    private final EpubImportProcessor epubImportProcessor;
    private final NotificationService notificationService;

    public ChapterAudioProcessor(ChapterService chapterService,
            EpubImportProcessor epubImportProcessor,
            NotificationService notificationService) {
        this.chapterService = chapterService;
        this.epubImportProcessor = epubImportProcessor;
        this.notificationService = notificationService;
    }

    @Transactional
    public void process(ChapterAudioMessage message) {
        try {
            TimeUnit.MICROSECONDS.sleep(100);
            if (message.getChapterId() == null) {
                logger.warn("Chapter ID is null in ChapterAudioMessage, skipping processing.");
                return;
            }
            Chapter chapter = chapterService.ensureChapterAudioGenerated(message.getChapterId());
            epubImportProcessor.markChapterAudioComplete(message.getJobId());
            notifySuccess(message, chapter);
        } catch (Exception ex) {
            logger.error("Failed to generate audio for chapter {}", message.getChapterId(), ex);
            epubImportProcessor.markJobFailed(message.getJobId(),
                    "Audio generation failed for chapter " + message.getChapterId() + ": " + ex.getMessage());
            notifyFailure(message, ex.getMessage());
        }
    }

    @CacheEvict
    private void notifySuccess(ChapterAudioMessage message, Chapter chapter) {
        if (message.getUserId() == null) {
            return;
        }
        CreateNotificationDTO dto = CreateNotificationDTO.builder()
                .userId(message.getUserId())
                .title("Chapter audio ready")
                .message(String.format("Audio for %s - Chapter %d is ready.",
                        chapter.getNovel().getTitle(), chapter.getChapterNumber()))
                .type(NotificationType.CHAPTER_UPDATE)
                .reference(chapter.getNovel().getSlug() + "/chapters/" + chapter.getChapterNumber())
                .build();
        try {
            notificationService.createNotification(dto);
        } catch (Exception ex) {
            logger.error("Failed to send audio ready notification for chapter {}", chapter.getId(), ex);
        }
    }

    private void notifyFailure(ChapterAudioMessage message, String reason) {
        if (message.getUserId() == null) {
            return;
        }
        CreateNotificationDTO dto = CreateNotificationDTO.builder()
                .userId(message.getUserId())
                .title("Chapter audio failed")
                .message(reason == null ? "Unable to generate chapter audio." : reason)
                .type(NotificationType.SYSTEM)
                .build();
        try {
            notificationService.createNotification(dto);
        } catch (Exception ex) {
            logger.error("Failed to send audio failure notification for chapter {}", message.getChapterId(), ex);
        }
    }
}
