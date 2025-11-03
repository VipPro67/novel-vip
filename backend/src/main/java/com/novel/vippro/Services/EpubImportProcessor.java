package com.novel.vippro.Services;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Messaging.AsyncTaskPublisher;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.EpubImportJob;
import com.novel.vippro.Models.EpubImportStatus;
import com.novel.vippro.Models.EpubImportType;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NotificationType;
import com.novel.vippro.Repository.EpubImportJobRepository;
import com.novel.vippro.Repository.FileMetadataRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Utils.EpubParseResult;
import com.novel.vippro.Utils.EpubParser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class EpubImportProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EpubImportProcessor.class);

    private final EpubImportJobRepository jobRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final FileService fileService;
    private final ChapterService chapterService;
    private final NovelService novelService;
    private final NovelRepository novelRepository;
    private final SearchService searchService;
    private final AsyncTaskPublisher asyncTaskPublisher;
    private final NotificationService notificationService;

    public EpubImportProcessor(EpubImportJobRepository jobRepository,
            FileMetadataRepository fileMetadataRepository,
            FileStorageService fileStorageService,
            FileService fileService,
            ChapterService chapterService,
            NovelService novelService,
            NovelRepository novelRepository,
            SearchService searchService,
            AsyncTaskPublisher asyncTaskPublisher,
            NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
        this.fileService = fileService;
        this.chapterService = chapterService;
        this.novelService = novelService;
        this.novelRepository = novelRepository;
        this.searchService = searchService;
        this.asyncTaskPublisher = asyncTaskPublisher;
        this.notificationService = notificationService;
    }

    @Transactional
    public void process(EpubImportMessage message) {
        try {
            TimeUnit.SECONDS.sleep(1);
            EpubImportJob job = jobRepository.findById(message.getJobId()).orElse(null);
        if (job == null) {
            logger.warn("Received EPUB import message for unknown job {}", message.getJobId());
            return;
        }

        try {
            logger.info("Processing EPUB import job {}", job.getId());
            job.setStatus(EpubImportStatus.PARSING);
            job.setStatusMessage("Parsing EPUB file");
            jobRepository.save(job);

            FileMetadata importFile = resolveFile(message, job);
            byte[] epubBytes = fileStorageService.downloadFile(importFile.getPublicId());
            EpubParseResult parsed = EpubParser.parse(epubBytes);
            int chapterCount = parsed.getChapters() == null ? 0 : parsed.getChapters().size();
            job.setTotalChapters(chapterCount);
            job.setChaptersProcessed(0);
            job.setAudioCompleted(0);
            jobRepository.save(job);

            if (job.getType() == EpubImportType.CREATE_NOVEL) {
                processCreateNovel(job, parsed);
            } else {
                processAppendChapters(job, parsed);
            }

            job.setStatus(EpubImportStatus.CHAPTERS_CREATED);
            job.setStatusMessage(String.format("Created %d chapters", job.getChaptersProcessed()));
            jobRepository.save(job);

            if (job.getTotalChapters() == 0) {
                markJobCompleted(job, "EPUB import completed. No chapters detected.");
            } else {
                job.setStatus(EpubImportStatus.WAITING_FOR_AUDIO);
                job.setStatusMessage(String.format("Waiting for audio generation (%d chapters queued)",
                        job.getTotalChapters()));
                jobRepository.save(job);
            }
        } catch (Exception ex) {
            logger.error("Failed to process EPUB import job {}", job.getId(), ex);
            job.setStatus(EpubImportStatus.FAILED);
            job.setStatusMessage("Failed: " + ex.getMessage());
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
            notifyUser(job, "EPUB import failed", ex.getMessage(), NotificationType.SYSTEM, job.getSlug());
        }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private FileMetadata resolveFile(EpubImportMessage message, EpubImportJob job) {
        FileMetadata file = job.getImportFile();
        if (file == null && message.getFileMetadataId() != null) {
            file = fileMetadataRepository.findById(message.getFileMetadataId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Import file metadata not found for job " + job.getId()));
            job.setImportFile(file);
        }
        if (file == null) {
            throw new IllegalStateException("No import file associated with job " + job.getId());
        }
        return file;
    }

    private void processCreateNovel(EpubImportJob job, EpubParseResult parsed) {
        Novel novel = novelService.saveNovelInitial(parsed, job.getSlug(), job.getRequestedStatus());
        job.setNovelId(novel.getId());
        job.setSlug(novel.getSlug());
        jobRepository.save(job);

        if (parsed.getCoverImage() != null) {
            try {
                FileMetadata cover = fileService.uploadFile(parsed.getCoverImage(), parsed.getCoverImageName(),
                        "image/jpeg", "cover");
                novel.setCoverImage(cover);
                novelRepository.save(novel);
            } catch (Exception ex) {
                logger.warn("Failed to upload EPUB cover for novel {}: {}", novel.getId(), ex.getMessage());
            }
        }

        int idx = 1;
        if (parsed.getChapters() != null) {
            for (var chapterData : parsed.getChapters()) {
                Chapter chapter = createChapter(job, novel.getId(), idx, chapterData.getTitle(),
                        chapterData.getContentHtml());
                idx++;
                //enqueueAudio(job, novel, chapter);
            }
        }

        Novel refreshed = novelRepository.findById(novel.getId()).orElse(novel);
        searchService.indexNovels(List.of(refreshed));
    }

    private void processAppendChapters(EpubImportJob job, EpubParseResult parsed) {
        if (job.getNovelId() == null) {
            throw new IllegalStateException("Append chapters job missing novel id");
        }
        Novel novel = novelRepository.findById(job.getNovelId())
                .orElseThrow(() -> new IllegalStateException("Novel not found for append job " + job.getId()));
        int lastChapterNumber = 0;
        try {
            lastChapterNumber = chapterService.getLastChapterNumber(novel.getId());
        } catch (Exception ex) {
            logger.info("Novel {} has no existing chapters. Starting from 0.", novel.getId());
        }

        int idx = lastChapterNumber + 1;
        if (parsed.getChapters() != null) {
            for (var chapterData : parsed.getChapters()) {
                Chapter chapter = createChapter(job, novel.getId(), idx, chapterData.getTitle(),
                        chapterData.getContentHtml());
                idx++;
                //enqueueAudio(job, novel, chapter);
            }
        }

        Novel refreshed = novelRepository.findById(novel.getId()).orElse(novel);
        searchService.indexNovels(List.of(refreshed));
    }

    private Chapter createChapter(EpubImportJob job, UUID novelId, int chapterNumber, String title, String htmlContent) {
        CreateChapterDTO dto = new CreateChapterDTO();
        dto.setNovelId(novelId);
        dto.setChapterNumber(chapterNumber);
        String effectiveTitle = (title == null || title.isBlank()) ? "Chapter " + chapterNumber : title;
        dto.setTitle(effectiveTitle);
        dto.setContentHtml(htmlContent == null ? "" : htmlContent);
        dto.setFormat(CreateChapterDTO.ContentFormat.HTML);
        Chapter chapter = chapterService.createChapter(dto);
        job.setChaptersProcessed(job.getChaptersProcessed() + 1);
        job.setStatusMessage(String.format("Created chapter %d", chapterNumber));
        jobRepository.save(job);
        return chapter;
    }

    private void enqueueAudio(EpubImportJob job, Novel novel, Chapter chapter) {
        ChapterAudioMessage audioMessage = ChapterAudioMessage.builder()
                .chapterId(chapter.getId())
                .jobId(job.getId())
                .novelId(novel.getId())
                .novelSlug(novel.getSlug())
                .chapterNumber(chapter.getChapterNumber())
                .userId(job.getUserId())
                .build();
        asyncTaskPublisher.publishChapterAudio(audioMessage);
    }

    private void markJobCompleted(EpubImportJob job, String message) {
        job.setStatus(EpubImportStatus.COMPLETED);
        job.setStatusMessage(message);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
        notifyUser(job, "EPUB import completed", message, NotificationType.SYSTEM, job.getSlug());
    }

    private void notifyUser(EpubImportJob job, String title, String message, NotificationType type, String reference) {
        if (job.getUserId() == null) {
            return;
        }
        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setUserId(job.getUserId());
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setType(type);
        dto.setReference(reference);
        try {
            notificationService.createNotification(dto);
        } catch (Exception ex) {
            logger.error("Failed to push notification for job {}", job.getId(), ex);
        }
    }

    @Transactional
    public void markChapterAudioComplete(UUID jobId) {
        if (jobId == null) {
            return;
        }
        boolean updated = false;
        for (int attempt = 0; attempt < 3 && !updated; attempt++) {
            try {
                EpubImportJob job = jobRepository.findById(jobId).orElse(null);
                if (job == null) {
                    return;
                }
                job.setAudioCompleted(job.getAudioCompleted() + 1);
                if (job.getAudioCompleted() >= job.getTotalChapters()) {
                    markJobCompleted(job, "EPUB import and audio generation completed");
                } else {
                    job.setStatusMessage(String.format("Audio generated for %d/%d chapters",
                            job.getAudioCompleted(), job.getTotalChapters()));
                    jobRepository.save(job);
                }
                updated = true;
            } catch (OptimisticLockingFailureException ex) {
                logger.warn("Optimistic lock while updating job {} progress. Retrying...", jobId);
            }
        }
    }

    @Transactional
    public void markJobFailed(UUID jobId, String reason) {
        if (jobId == null) {
            return;
        }
        EpubImportJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(EpubImportStatus.FAILED);
        job.setStatusMessage(reason);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
        notifyUser(job, "EPUB import failed", reason, NotificationType.SYSTEM, job.getSlug());
    }
}
