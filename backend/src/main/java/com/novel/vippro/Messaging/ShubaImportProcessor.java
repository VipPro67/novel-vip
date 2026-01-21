package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.NovelSource.ShubaChapterDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import com.novel.vippro.Models.*;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.SystemJobRepository;
import com.novel.vippro.Repository.NovelSourceRepository;
import com.novel.vippro.Services.*;
import com.novel.vippro.Services.ShubaNovelCrawlerService.ChapterInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ShubaImportProcessor {

    private static final int CHAPTER_BATCH_SIZE = 50;
    
    private final SystemJobRepository jobRepository;
    private final NovelSourceRepository novelSourceRepository;
    private final NovelRepository novelRepository;
    private final ChapterService chapterService;
    private final NovelService novelService;
    private final ShubaNovelCrawlerService crawlerService;
    private final GeminiTranslationService translationService;
    private final SearchService searchService;
    private final NotificationService notificationService;

    @Transactional
    public void process(ShubaImportMessage message) {
        SystemJob job = null;
        NovelSource novelSource = null;
        
        try {
            job = jobRepository.findById(message.getJobId()).orElse(null);
            if (job == null) {
                log.warn("Received Shuba import message for unknown job {}", message.getJobId());
                return;
            }

            novelSource = novelSourceRepository.findById(message.getNovelSourceId()).orElse(null);
            if (novelSource == null) {
                failJob(job, "Novel source not found");
                return;
            }

            log.info("Processing Shuba import job {} for novel source {}", job.getId(), novelSource.getId());
            
            // Update job status
            job.setStatus(SystemJobStatus.PARSING);
            job.setStatusMessage("Fetching chapter list from 69shuba");
            jobRepository.save(job);

            // Update source status
            novelSource.setSyncStatus(SyncStatus.SYNCING);
            novelSourceRepository.save(novelSource);

            // Fetch chapter list
            List<ChapterInfo> allChapters = crawlerService.fetchChapterList(novelSource.getSourceUrl());
            
            if (allChapters.isEmpty()) {
                failJob(job, "No chapters found at source URL");
                novelSource.setSyncStatus(SyncStatus.FAILED);
                novelSource.setErrorMessage("No chapters found");
                novelSourceRepository.save(novelSource);
                return;
            }

            // Determine which chapters to fetch
            int startIndex = 0;
            int endIndex = allChapters.size();
            
            if (!Boolean.TRUE.equals(message.getFullImport())) {
                // Incremental import: fetch only new chapters
                Integer lastSynced = novelSource.getLastSyncedChapter();
                if (lastSynced != null && lastSynced > 0) {
                    startIndex = lastSynced; // Start from next chapter
                }
                
                if (message.getStartChapter() != null) {
                    startIndex = Math.max(startIndex, message.getStartChapter() - 1);
                }
                if (message.getEndChapter() != null) {
                    endIndex = Math.min(endIndex, message.getEndChapter());
                }
            } else if (message.getStartChapter() != null || message.getEndChapter() != null) {
                // Manual range specified
                if (message.getStartChapter() != null) {
                    startIndex = message.getStartChapter() - 1;
                }
                if (message.getEndChapter() != null) {
                    endIndex = message.getEndChapter();
                }
            }

            List<ChapterInfo> chaptersToFetch = allChapters.subList(
                Math.max(0, startIndex), 
                Math.min(allChapters.size(), endIndex)
            );

            if (chaptersToFetch.isEmpty()) {
                log.info("No new chapters to import for novel source {}", novelSource.getId());
                completeJob(job, novelSource, "No new chapters to import");
                return;
            }

            job.setTotalChapters(chaptersToFetch.size());
            job.setChaptersProcessed(0);
            job.setStatusMessage(String.format("Importing %d chapters", chaptersToFetch.size()));
            jobRepository.save(job);

            // Process chapters in batches
            Novel novel = novelRepository.findById(message.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found"));
            
            int processedCount = 0;
            int highestChapterNumber = novel.getTotalChapters() != null ? novel.getTotalChapters() : 0;
            
            for (int i = 0; i < chaptersToFetch.size(); i += CHAPTER_BATCH_SIZE) {
                int batchEnd = Math.min(i + CHAPTER_BATCH_SIZE, chaptersToFetch.size());
                List<ChapterInfo> batch = chaptersToFetch.subList(i, batchEnd);
                
                log.info("Processing batch {}-{} of {}", i + 1, batchEnd, chaptersToFetch.size());
                
                for (ChapterInfo chapterInfo : batch) {
                    try {
                        // Fetch chapter content
                        ShubaChapterDTO rawChapter = crawlerService.fetchChapter(chapterInfo);
                        
                        // Translate to Vietnamese
                        job.setStatusMessage(String.format("Translating chapter %d: %s", 
                            rawChapter.getChapterNumber(), rawChapter.getTitle()));
                        jobRepository.save(job);
                        
                        String translatedContent = translationService.translateHtmlToVietnamese(rawChapter.getContentHtml());
                        String translatedTitle = translationService.translateText(rawChapter.getTitle());
                        
                        // Create chapter
                        int targetChapterNumber = highestChapterNumber + rawChapter.getChapterNumber();
                        CreateChapterDTO chapterDTO = new CreateChapterDTO(
                            message.getNovelId(),
                            targetChapterNumber,
                            translatedTitle,
                            translatedContent,
                            null, // audio
                            true  // skipAudioGeneration
                        );
                        
                        chapterService.createChapter(chapterDTO, message.getUserId());
                        
                        processedCount++;
                        job.setChaptersProcessed(processedCount);
                        job.setStatusMessage(String.format("Imported %d/%d chapters", processedCount, chaptersToFetch.size()));
                        jobRepository.save(job);
                        
                        // Update last synced chapter
                        novelSource.setLastSyncedChapter(chapterInfo.getChapterNumber());
                        novelSourceRepository.save(novelSource);
                        
                        // Rate limiting
                        Thread.sleep(500);
                        
                    } catch (Exception e) {
                        log.error("Failed to process chapter {}: {}", chapterInfo.getChapterNumber(), e.getMessage());
                        // Continue with next chapter
                    }
                }
            }

            // Update novel chapter count
            novelService.updateNovelChapterCount(message.getNovelId());
            
            // Index for search
            try {
                searchService.indexNovel(novel);
            } catch (Exception e) {
                log.warn("Failed to index novel after import: {}", e.getMessage());
            }

            // Complete the job
            completeJob(job, novelSource, String.format("Successfully imported %d chapters", processedCount));
            
            // Send notification
            sendNotification(message.getUserId(), novel.getTitle(), processedCount);

        } catch (Exception e) {
            log.error("Error processing Shuba import job {}: {}", message.getJobId(), e.getMessage(), e);
            if (job != null) {
                failJob(job, "Import failed: " + e.getMessage());
            }
            if (novelSource != null) {
                novelSource.setSyncStatus(SyncStatus.FAILED);
                novelSource.setErrorMessage(e.getMessage());
                novelSource.setConsecutiveFailures(novelSource.getConsecutiveFailures() + 1);
                novelSourceRepository.save(novelSource);
            }
        }
    }

    private void completeJob(SystemJob job, NovelSource source, String message) {
        job.setStatus(SystemJobStatus.COMPLETED);
        job.setStatusMessage(message);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
        
        source.setSyncStatus(SyncStatus.SUCCESS);
        source.setLastSyncTime(Instant.now());
        source.setConsecutiveFailures(0);
        source.setNextSyncTime(Instant.now().plusSeconds(source.getSyncIntervalMinutes() * 60L));
        source.setErrorMessage(null);
        novelSourceRepository.save(source);
        
        log.info("Completed Shuba import job {}", job.getId());
    }

    private void failJob(SystemJob job, String errorMessage) {
        job.setStatus(SystemJobStatus.FAILED);
        job.setStatusMessage(errorMessage);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
        log.error("Failed Shuba import job {}: {}", job.getId(), errorMessage);
    }

    private void sendNotification(UUID userId, String novelTitle, int chapterCount) {
        try {
            CreateNotificationDTO notification = new CreateNotificationDTO(
                userId,
                NotificationType.SYSTEM,
                "Shuba Import Complete",
                String.format("Successfully imported %d chapters for novel: %s", chapterCount, novelTitle),
                null,
                null
            );
            notificationService.createNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }
}
