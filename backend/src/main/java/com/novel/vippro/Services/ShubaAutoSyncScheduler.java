package com.novel.vippro.Services;

import com.novel.vippro.DTO.NovelSource.NovelSourceDTO;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import com.novel.vippro.Models.*;
import com.novel.vippro.Repository.SystemJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "shuba.sync.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class ShubaAutoSyncScheduler {

    private final NovelSourceService novelSourceService;
    private final SystemJobRepository systemJobRepository;
    private final MessagePublisher messagePublisher;

    /**
     * Check for novel sources that need syncing every 15 minutes
     */
    @Scheduled(fixedDelayString = "${shuba.sync.interval:900000}") // Default: 15 minutes
    public void scheduledSync() {
        log.info("Running scheduled Shuba sync check");
        
        try {
            List<NovelSourceDTO> dueForSync = novelSourceService.getSourcesDueForSync();
            
            if (dueForSync.isEmpty()) {
                log.debug("No novel sources due for sync");
                return;
            }
            
            log.info("Found {} novel sources due for sync", dueForSync.size());
            
            for (NovelSourceDTO source : dueForSync) {
                try {
                    triggerSync(source);
                } catch (Exception e) {
                    log.error("Failed to trigger sync for novel source {}: {}", 
                        source.id(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled sync check: {}", e.getMessage(), e);
        }
    }

    private void triggerSync(NovelSourceDTO source) {
        log.info("Triggering automatic sync for novel source: {} (Novel: {})", 
            source.id(), source.novelTitle());
        
        // Create system job
        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.SHUBA_IMPORT);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setUserId(source.createdBy());
        job.setNovelId(source.novelId());
        job.setStatusMessage("Queued for automatic sync");
        
        SystemJob savedJob = systemJobRepository.save(job);
        
        // Create and publish message (incremental sync)
        ShubaImportMessage message = ShubaImportMessage.builder()
            .jobId(savedJob.getId())
            .userId(savedJob.getUserId())
            .novelId(source.novelId())
            .novelSourceId(source.id())
            .fullImport(false) // Incremental sync
            .build();
        
        messagePublisher.publishShubaImport(message);
        
        log.info("Queued sync job {} for novel source {}", savedJob.getId(), source.id());
    }
}
