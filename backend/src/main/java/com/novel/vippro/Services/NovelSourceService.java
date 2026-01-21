package com.novel.vippro.Services;

import com.novel.vippro.DTO.NovelSource.*;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.NovelSourceMapper;
import com.novel.vippro.Models.*;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.NovelSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NovelSourceService {
    
    private final NovelSourceRepository novelSourceRepository;
    private final NovelRepository novelRepository;
    private final NovelSourceMapper mapper;
    
    @Transactional
    public NovelSourceDTO createNovelSource(CreateNovelSourceDTO dto) {
        log.info("Creating novel source for novel ID: {}", dto.novelId());
        
        // Check if novel exists
        Novel novel = novelRepository.findById(dto.novelId())
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
        
        // Check if source already exists
        if (novelSourceRepository.existsByNovelIdAndSourceUrl(dto.novelId(), dto.sourceUrl())) {
            throw new IllegalArgumentException("Novel source already exists for this URL");
        }
        
        NovelSource source = new NovelSource();
        source.setNovel(novel);
        source.setSourceUrl(dto.sourceUrl());
        source.setSourceId(dto.sourceId());
        source.setSourcePlatform(dto.sourcePlatform() != null ? dto.sourcePlatform() : "69shuba");
        source.setSyncIntervalMinutes(dto.syncIntervalMinutes() != null ? dto.syncIntervalMinutes() : 60);
        source.setEnabled(true);
        source.setSyncStatus(SyncStatus.IDLE);
        source.setConsecutiveFailures(0);
        source.setNextSyncTime(Instant.now());
        
        NovelSource saved = novelSourceRepository.save(source);
        log.info("Created novel source with ID: {}", saved.getId());
        
        return mapper.toDTO(saved);
    }
    
    @Transactional
    public NovelSourceDTO updateNovelSource(UUID id, UpdateNovelSourceDTO dto) {
        log.info("Updating novel source: {}", id);
        
        NovelSource source = novelSourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Novel source not found"));
        
        if (dto.enabled() != null) {
            source.setEnabled(dto.enabled());
        }
        
        if (dto.syncIntervalMinutes() != null) {
            source.setSyncIntervalMinutes(dto.syncIntervalMinutes());
        }
        
        if (dto.sourceId() != null) {
            source.setSourceId(dto.sourceId());
        }
        
        NovelSource updated = novelSourceRepository.save(source);
        return mapper.toDTO(updated);
    }
    
    @Transactional(readOnly = true)
    public NovelSourceDTO getNovelSource(UUID id) {
        NovelSource source = novelSourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Novel source not found"));
        return mapper.toDTO(source);
    }
    
    @Transactional(readOnly = true)
    public List<NovelSourceDTO> getNovelSourcesByNovelId(UUID novelId) {
        List<NovelSource> sources = novelSourceRepository.findByNovelId(novelId);
        return sources.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NovelSourceDTO> getAllNovelSources() {
        List<NovelSource> sources = novelSourceRepository.findAll();
        return sources.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NovelSourceDTO> getSourcesDueForSync() {
        List<NovelSource> sources = novelSourceRepository.findDueForSync(Instant.now());
        return sources.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateSyncStatus(UUID sourceId, SyncStatus status, String errorMessage) {
        NovelSource source = novelSourceRepository.findById(sourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel source not found"));
        
        source.setSyncStatus(status);
        source.setErrorMessage(errorMessage);
        
        if (status == SyncStatus.SUCCESS) {
            source.setLastSyncTime(Instant.now());
            source.setConsecutiveFailures(0);
            source.setNextSyncTime(Instant.now().plusSeconds(source.getSyncIntervalMinutes() * 60L));
        } else if (status == SyncStatus.FAILED) {
            source.setConsecutiveFailures(source.getConsecutiveFailures() + 1);
            // Exponential backoff for failures
            long backoffMinutes = Math.min(source.getSyncIntervalMinutes() * (1L << source.getConsecutiveFailures()), 1440);
            source.setNextSyncTime(Instant.now().plusSeconds(backoffMinutes * 60));
        }
        
        novelSourceRepository.save(source);
    }
    
    @Transactional
    public void updateLastSyncedChapter(UUID sourceId, int chapterNumber) {
        NovelSource source = novelSourceRepository.findById(sourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel source not found"));
        
        source.setLastSyncedChapter(chapterNumber);
        novelSourceRepository.save(source);
    }
    
    @Transactional
    public void deleteNovelSource(UUID id) {
        log.info("Deleting novel source: {}", id);
        novelSourceRepository.deleteById(id);
    }
}
