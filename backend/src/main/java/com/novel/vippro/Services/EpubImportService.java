package com.novel.vippro.Services;

import com.novel.vippro.DTO.System.SystemJobDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import com.novel.vippro.Models.EpubImportType;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.SystemJob;
import com.novel.vippro.Models.SystemJobStatus;
import com.novel.vippro.Models.SystemJobType;
import com.novel.vippro.Repository.SystemJobRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EpubImportService {

    private static final Logger logger = LoggerFactory.getLogger(EpubImportService.class);

    private final FileService fileService;
    private final SystemJobRepository jobRepository;
    private final Mapper mapper;
    private final MessagePublisher messagePublisher;
    private final NovelRepository novelRepository;

    public EpubImportService(FileService fileService,
            SystemJobRepository jobRepository,
            Mapper mapper,
            MessagePublisher messagePublisher,
            NovelRepository novelRepository) {
        this.fileService = fileService;
        this.jobRepository = jobRepository;
        this.mapper = mapper;
        this.messagePublisher = messagePublisher;
        this.novelRepository = novelRepository;
    }

    @Transactional
    public SystemJobDTO queueNewNovelImport(MultipartFile epub, String slug, String status) {
        UUID userId = requireAuthenticatedUser();
        logger.info("Queueing EPUB import for new novel slug={} by user={}", slug, userId);
        FileMetadata metadata;
        try {
            String publicId = String.format("novels/%s/epubs/", slug);
            metadata = fileService.uploadFileWithPublicId(epub.getBytes(), publicId, epub.getOriginalFilename(), epub.getContentType(), "epub");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read EPUB file content", e);
        }
        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.EPUB_IMPORT);
        job.setImportType(EpubImportType.CREATE_NOVEL);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setImportFile(metadata);
        job.setUserId(userId);
        job.setSlug(slug);
        job.setRequestedStatus(status);
        job.setStatusMessage("Queued for processing");
        job.setOriginalFileName(epub.getOriginalFilename());
        job = jobRepository.save(job);

        publishAfterCommit(job);
        return mapper.SystemJobToDTO(job);
    }

    @Transactional
    public SystemJobDTO queueChaptersImport(UUID novelId, MultipartFile epub) {
        UUID userId = requireAuthenticatedUser();
        var novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("Novel not found with id " + novelId));
        logger.info("Queueing EPUB import for novel {} to append chapters by user={}.", novelId, userId);

        FileMetadata metadata;
        try {
            String publicId = String.format("novels/%s/epubs/", novel.getSlug());
            metadata = fileService.uploadFileWithPublicId(epub.getBytes(), publicId, epub.getOriginalFilename(), epub.getContentType(), "epub");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read EPUB file content", e);
        }


        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.EPUB_IMPORT);
        job.setImportType(EpubImportType.APPEND_CHAPTERS);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setImportFile(metadata);
        job.setUserId(userId);
        job.setNovelId(novelId);
        job.setSlug(novel.getSlug());
        job.setStatusMessage("Queued for processing");
        job.setOriginalFileName(epub.getOriginalFilename());
        job = jobRepository.save(job);

        publishAfterCommit(job);
        return mapper.SystemJobToDTO(job);
    }

    private void publish(SystemJob job) {
        EpubImportMessage message = EpubImportMessage.builder()
                .jobId(job.getId())
                .userId(job.getUserId())
                .novelId(job.getNovelId())
                .fileMetadataId(job.getImportFile() != null ? job.getImportFile().getId() : null)
                .slug(job.getSlug())
                .requestedStatus(job.getRequestedStatus())
                .originalFileName(job.getOriginalFileName())
                .type(job.getImportType())
                .build();
        messagePublisher.publishEpubImport(message);
    }

    private void publishAfterCommit(SystemJob job) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            SystemJob persistedJob = job;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(persistedJob);
                }
            });
            return;
        }
        publish(job);
    }

    private UUID requireAuthenticatedUser() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("Authenticated user is required for EPUB imports");
        }
        return userId;
    }
}
