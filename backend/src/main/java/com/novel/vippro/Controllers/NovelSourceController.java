package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.NovelSource.*;
import com.novel.vippro.Messaging.MessagePublisher;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;
import com.novel.vippro.Models.*;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Repository.SystemJobRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.NovelSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin/novel-sources")
@Tag(name = "Novel Source Management", description = "Manage novel sources for automatic importing")
@RequiredArgsConstructor
@Slf4j
public class NovelSourceController {

    private final NovelSourceService novelSourceService;
    private final SystemJobRepository systemJobRepository;
    private final MessagePublisher messagePublisher;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Create novel source", description = "Add a new novel source for automatic importing")
    public ControllerResponse<NovelSourceDTO> createNovelSource(
            @Valid @RequestBody CreateNovelSourceDTO dto) {
        NovelSourceDTO created = novelSourceService.createNovelSource(dto);
        return ControllerResponse.success("Novel source created successfully", created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Update novel source", description = "Update an existing novel source")
    public ControllerResponse<NovelSourceDTO> updateNovelSource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNovelSourceDTO dto) {
        NovelSourceDTO updated = novelSourceService.updateNovelSource(id, dto);
        return ControllerResponse.success("Novel source updated successfully", updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Get novel source", description = "Get a specific novel source by ID")
    public ControllerResponse<NovelSourceDTO> getNovelSource(@PathVariable UUID id) {
        NovelSourceDTO source = novelSourceService.getNovelSource(id);
        return ControllerResponse.success("Novel source retrieved successfully", source);
    }

    @GetMapping("/novel/{novelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Get novel sources by novel", description = "Get all sources for a specific novel")
    public ControllerResponse<List<NovelSourceDTO>> getNovelSourcesByNovelId(@PathVariable UUID novelId) {
        List<NovelSourceDTO> sources = novelSourceService.getNovelSourcesByNovelId(novelId);
        return ControllerResponse.success("Novel sources retrieved successfully", sources);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all novel sources", description = "Get all novel sources in the system")
    public ControllerResponse<List<NovelSourceDTO>> getAllNovelSources() {
        List<NovelSourceDTO> sources = novelSourceService.getAllNovelSources();
        return ControllerResponse.success("Novel sources retrieved successfully", sources);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Delete novel source", description = "Delete a novel source")
    public ControllerResponse<Void> deleteNovelSource(@PathVariable UUID id) {
        novelSourceService.deleteNovelSource(id);
        return ControllerResponse.success("Novel source deleted successfully", null);
    }

    @PostMapping("/{id}/sync")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Trigger manual sync", description = "Manually trigger a sync for a novel source")
    public ControllerResponse<UUID> triggerSync(
            @PathVariable UUID id,
            @RequestBody(required = false) ShubaImportRequestDTO requestDTO) {
        
        NovelSourceDTO source = novelSourceService.getNovelSource(id);
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();

        // Create system job
        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.SHUBA_IMPORT);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setUserId(currentUserId);
        job.setNovelId(source.novelId());
        job.setStatusMessage("Queued for import");
        
        SystemJob savedJob = systemJobRepository.save(job);
        
        // Create and publish message
        ShubaImportMessage message = ShubaImportMessage.builder()
            .jobId(savedJob.getId())
            .userId(savedJob.getUserId())
            .novelId(source.novelId())
            .novelSourceId(id)
            .startChapter(requestDTO != null ? requestDTO.startChapter() : null)
            .endChapter(requestDTO != null ? requestDTO.endChapter() : null)
            .fullImport(requestDTO != null ? requestDTO.fullImport() : false)
            .build();
        
        messagePublisher.publishShubaImport(message);
        
        log.info("Triggered sync for novel source {} with job {}", id, savedJob.getId());
        
        return ControllerResponse.success("Sync job queued successfully", savedJob.getId());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    @Operation(summary = "Quick import", description = "Create source and immediately trigger import")
    public ControllerResponse<UUID> quickImport(
            @Valid @RequestBody CreateNovelSourceDTO createDTO) {
        
        // Create the source
        NovelSourceDTO source = novelSourceService.createNovelSource(createDTO);
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        
        // Create system job
        SystemJob job = new SystemJob();
        job.setJobType(SystemJobType.SHUBA_IMPORT);
        job.setStatus(SystemJobStatus.QUEUED);
        job.setUserId(currentUserId);
        job.setNovelId(source.novelId());
        job.setStatusMessage("Queued for initial import");
        
        SystemJob savedJob = systemJobRepository.save(job);
        
        // Create and publish message
        ShubaImportMessage message = ShubaImportMessage.builder()
            .jobId(savedJob.getId())
            .userId(savedJob.getUserId())
            .novelId(source.novelId())
            .novelSourceId(source.id())
            .fullImport(true)
            .build();
        
        messagePublisher.publishShubaImport(message);
        
        log.info("Created novel source {} and triggered import job {}", source.id(), savedJob.getId());
        
        return ControllerResponse.success("Import job queued successfully", savedJob.getId());
    }
}
