package com.novel.vippro.Services;

import com.novel.vippro.DTO.Epub.EpubImportJobDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Messaging.AsyncTaskPublisher;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import com.novel.vippro.Models.EpubImportJob;
import com.novel.vippro.Models.EpubImportStatus;
import com.novel.vippro.Models.EpubImportType;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Repository.EpubImportJobRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Security.UserDetailsImpl;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EpubImportService {

    private static final Logger logger = LoggerFactory.getLogger(EpubImportService.class);

    private final FileService fileService;
    private final EpubImportJobRepository jobRepository;
    private final Mapper mapper;
    private final AsyncTaskPublisher asyncTaskPublisher;
    private final NovelRepository novelRepository;

    public EpubImportService(FileService fileService,
            EpubImportJobRepository jobRepository,
            Mapper mapper,
            AsyncTaskPublisher asyncTaskPublisher,
            NovelRepository novelRepository) {
        this.fileService = fileService;
        this.jobRepository = jobRepository;
        this.mapper = mapper;
        this.asyncTaskPublisher = asyncTaskPublisher;
        this.novelRepository = novelRepository;
    }

    @Transactional
    public EpubImportJobDTO queueNewNovelImport(MultipartFile epub, String slug, String status) {
        UUID userId = requireAuthenticatedUser();
        logger.info("Queueing EPUB import for new novel slug={} by user={}", slug, userId);
        FileMetadata metadata = fileService.uploadFile(epub, "epub");

        EpubImportJob job = new EpubImportJob();
        job.setType(EpubImportType.CREATE_NOVEL);
        job.setStatus(EpubImportStatus.QUEUED);
        job.setImportFile(metadata);
        job.setUserId(userId);
        job.setSlug(slug);
        job.setRequestedStatus(status);
        job.setStatusMessage("Queued for processing");
        job.setOriginalFileName(epub.getOriginalFilename());
        job = jobRepository.save(job);

        publish(job);
        return mapper.EpubImportJobToDTO(job);
    }

    @Transactional
    public EpubImportJobDTO queueChaptersImport(UUID novelId, MultipartFile epub) {
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


        EpubImportJob job = new EpubImportJob();
        job.setType(EpubImportType.APPEND_CHAPTERS);
        job.setStatus(EpubImportStatus.QUEUED);
        job.setImportFile(metadata);
        job.setUserId(userId);
        job.setNovelId(novelId);
        job.setSlug(novel.getSlug());
        job.setStatusMessage("Queued for processing");
        job.setOriginalFileName(epub.getOriginalFilename());
        job = jobRepository.save(job);

        publish(job);
        return mapper.EpubImportJobToDTO(job);
    }

    private void publish(EpubImportJob job) {
        EpubImportMessage message = EpubImportMessage.builder()
                .jobId(job.getId())
                .userId(job.getUserId())
                .novelId(job.getNovelId())
                .fileMetadataId(job.getImportFile() != null ? job.getImportFile().getId() : null)
                .slug(job.getSlug())
                .requestedStatus(job.getRequestedStatus())
                .originalFileName(job.getOriginalFileName())
                .type(job.getType())
                .build();
        asyncTaskPublisher.publishEpubImport(message);
    }

    private UUID requireAuthenticatedUser() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("Authenticated user is required for EPUB imports");
        }
        return userId;
    }
}
