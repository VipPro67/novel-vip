package com.novel.vippro.Messaging;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.NovelSource.ShubaChapterDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import com.novel.vippro.Models.*;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.SystemJobRepository;
import com.novel.vippro.Repository.NovelSourceRepository;
import com.novel.vippro.Services.*;
import com.novel.vippro.Services.ShubaNovelCrawlerService.ChapterInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${translation.provider:groq}")
    private String translationProvider;
    
    private final SystemJobRepository jobRepository;
    private final NovelSourceRepository novelSourceRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterService chapterService;
    private final NovelService novelService;
    private final ShubaNovelCrawlerService crawlerService;
    private final GeminiTranslationService geminiTranslationService;
    private final GroqTranslationService groqTranslationService;
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

            long startTime = System.currentTimeMillis();
            
            // Get existing chapters from database - only fetch chapter numbers for efficiency
            long dbQueryStart = System.currentTimeMillis();
            List<Integer> existingChapterNumbers = chapterRepository.findChapterNumbersByNovelId(message.getNovelId());
            Novel novel = novelRepository.findById(message.getNovelId())
                .orElseThrow(() -> new RuntimeException("Novel not found"));
            long dbQueryTime = System.currentTimeMillis() - dbQueryStart;
            log.info("Database query took {}ms (fetched {} chapter numbers)", dbQueryTime, existingChapterNumbers.size());
            
            // Create a set of existing chapter numbers for efficient lookup
            long setCreationStart = System.currentTimeMillis();
            java.util.Set<Integer> existingChapterNumberSet = new java.util.HashSet<>(existingChapterNumbers);
            
            int highestChapterNumber = existingChapterNumbers.isEmpty() ? 0 : 
                existingChapterNumbers.stream().max(Integer::compareTo).orElse(0);
            long setCreationTime = System.currentTimeMillis() - setCreationStart;
            log.info("Set creation and max finding took {}ms", setCreationTime);
            
            log.info("Current highest chapter number in database: {}, total existing: {}", 
                highestChapterNumber, existingChapterNumbers.size());
            
            // Determine which chapters to fetch based on range
            long rangeCalculationStart = System.currentTimeMillis();
            
            // Build a map for faster chapter lookup by chapter number
            java.util.Map<Integer, ChapterInfo> chapterMap = new java.util.HashMap<>();
            int minChapterNum = Integer.MAX_VALUE;
            int maxChapterNum = Integer.MIN_VALUE;
            
            for (ChapterInfo chapter : allChapters) {
                int chapterNum = chapter.getChapterNumber();
                chapterMap.put(chapterNum, chapter);
                minChapterNum = Math.min(minChapterNum, chapterNum);
                maxChapterNum = Math.max(maxChapterNum, chapterNum);
            }
            
            log.info("Source has chapters from {} to {}", minChapterNum, maxChapterNum);
            
            // Determine range based on import type
            Integer startChapterNum = null;
            Integer endChapterNum = null;
            
            if (!Boolean.TRUE.equals(message.getFullImport())) {
                // Incremental import: start from highest existing chapter + 1
                Integer lastSynced = novelSource.getLastSyncedChapter();
                if (lastSynced != null && lastSynced > 0) {
                    startChapterNum = lastSynced + 1;
                } else if (highestChapterNumber > 0) {
                    startChapterNum = highestChapterNumber + 1;
                }
                
                // Cap the end at the maximum available chapter from source
                endChapterNum = maxChapterNum;
                
                if (message.getStartChapter() != null) {
                    startChapterNum = message.getStartChapter();
                }
                if (message.getEndChapter() != null) {
                    endChapterNum = Math.min(message.getEndChapter(), maxChapterNum);
                }
            } else if (message.getStartChapter() != null || message.getEndChapter() != null) {
                // Manual range specified
                startChapterNum = message.getStartChapter();
                endChapterNum = message.getEndChapter() != null ? 
                    Math.min(message.getEndChapter(), maxChapterNum) : maxChapterNum;
            }
            
            log.info("Filtering chapters from {} to {}", startChapterNum, endChapterNum);
            
            // Collect chapters to fetch based on chapter numbers
            List<ChapterInfo> chaptersToFetch = new ArrayList<>();
            
            if (startChapterNum == null && endChapterNum == null) {
                // No range specified, check all chapters
                for (ChapterInfo chapter : allChapters) {
                    if (!existingChapterNumberSet.contains(chapter.getChapterNumber())) {
                        chaptersToFetch.add(chapter);
                    }
                }
            } else {
                // Range specified, only check chapters in range
                int start = startChapterNum != null ? startChapterNum : minChapterNum;
                int end = endChapterNum != null ? endChapterNum : maxChapterNum;
                
                for (int chapterNum = start; chapterNum <= end; chapterNum++) {
                    if (!existingChapterNumberSet.contains(chapterNum)) {
                        ChapterInfo chapter = chapterMap.get(chapterNum);
                        if (chapter != null) {
                            chaptersToFetch.add(chapter);
                        }
                    }
                }
            }
            
            long rangeCalculationTime = System.currentTimeMillis() - rangeCalculationStart;
            log.info("Range calculation and filtering took {}ms", rangeCalculationTime);
            
            long totalPreprocessingTime = System.currentTimeMillis() - startTime;
            log.info("Total preprocessing took {}ms (DB: {}ms, Set: {}ms, Filter: {}ms)", 
                totalPreprocessingTime, dbQueryTime, setCreationTime, rangeCalculationTime);

            if (chaptersToFetch.isEmpty()) {
                log.info("No new chapters to import for novel source {}", novelSource.getId());
                completeJob(job, novelSource, "No new chapters to import");
                return;
            }

            job.setTotalChapters(chaptersToFetch.size());
            job.setChaptersProcessed(0);
            job.setStatusMessage(String.format("Importing %d new chapters", chaptersToFetch.size()));
            jobRepository.save(job);
            
            int processedCount = 0;
            int statusUpdateInterval = 25; // Update status every 25 chapters instead of every chapter
            
            for (int i = 0; i < chaptersToFetch.size(); i += CHAPTER_BATCH_SIZE) {
                int batchEnd = Math.min(i + CHAPTER_BATCH_SIZE, chaptersToFetch.size());
                List<ChapterInfo> batch = chaptersToFetch.subList(i, batchEnd);
                
                log.info("Processing batch {}-{} of {}", i + 1, batchEnd, chaptersToFetch.size());
                
                for (ChapterInfo chapterInfo : batch) {
                    try {
                        // Fetch chapter content
                        ShubaChapterDTO rawChapter = crawlerService.fetchChapter(chapterInfo);
                        
                        // Log translation progress (don't save to DB every time)
                        if (processedCount % statusUpdateInterval == 0) {
                            job.setStatusMessage(String.format("Translating chapter %d: %s", 
                                rawChapter.getChapterNumber(), rawChapter.getTitle()));
                            jobRepository.save(job);
                        } else {
                            log.info("Translating chapter {}: {}", rawChapter.getChapterNumber(), rawChapter.getTitle());
                        }
                        
                        // Use configured translation provider
                        String translatedContent;
                        String translatedTitle;
                        
                        if ("groq".equalsIgnoreCase(translationProvider)) {
                            log.debug("Using Groq for translation");
                            translatedContent = groqTranslationService.translateHtmlToVietnamese(rawChapter.getContentHtml());
                            translatedTitle = groqTranslationService.translateText(rawChapter.getTitle());
                        } else {
                            log.debug("Using Gemini for translation");
                            translatedContent = geminiTranslationService.translateHtmlToVietnamese(rawChapter.getContentHtml());
                            translatedTitle = null;
                            if (translatedContent.contains("<p>") && translatedContent.contains("</p>")) {
                                String[] parts = translatedContent.split("<p>", 2);
                                if (parts.length > 1) {
                                    String[] titleParts = parts[1].split("</p>", 2);
                                    if (titleParts.length > 0) {
                                        translatedTitle = titleParts[0].replaceAll("<[^>]+>", "").trim();
                                    }
                                }
                            }
                            if (translatedTitle == null || translatedTitle.isEmpty()) {
                                translatedTitle = rawChapter.getTitle();
                            }
                        }
                        
                        CreateChapterDTO chapterDTO = CreateChapterDTO.builder()
                            .novelId(message.getNovelId())
                            .chapterNumber(rawChapter.getChapterNumber())
                            .title(translatedTitle)
                            .contentHtml(translatedContent)
                            .format(CreateChapterDTO.ContentFormat.HTML)
                            .build();
                        
                        chapterService.createChapter(chapterDTO);
                        
                        processedCount++;
                        
                        // Update progress in DB every statusUpdateInterval chapters
                        if (processedCount % statusUpdateInterval == 0) {
                            job.setChaptersProcessed(processedCount);
                            job.setStatusMessage(String.format("Imported %d/%d chapters", processedCount, chaptersToFetch.size()));
                            jobRepository.save(job);
                            
                            // Update last synced chapter
                            novelSource.setLastSyncedChapter(chapterInfo.getChapterNumber());
                            novelSourceRepository.save(novelSource);
                        }
                        
                        // Rate limiting
                        Thread.sleep(500);
                        
                    } catch (Exception e) {
                        log.error("Failed to process chapter {}: {}", chapterInfo.getChapterNumber(), e.getMessage(), e);
                        // Continue with next chapter
                    }
                }
                
                // Update at end of each batch
                job.setChaptersProcessed(processedCount);
                job.setStatusMessage(String.format("Imported %d/%d chapters", processedCount, chaptersToFetch.size()));
                jobRepository.save(job);
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
            log.info("Shuba import job {} completed successfully", job.getId());
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
            CreateNotificationDTO notification = CreateNotificationDTO.builder()
                .userId(userId)
                .title("Shuba Import Complete")
                .message(String.format("Successfully imported %d chapters for novel: %s", chapterCount, novelTitle))
                .type(NotificationType.SYSTEM)
                .reference(null)
                .build();
            notificationService.createNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }
}
