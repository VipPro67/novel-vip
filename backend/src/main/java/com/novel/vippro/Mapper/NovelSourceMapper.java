package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.NovelSource.NovelSourceDTO;
import com.novel.vippro.Models.NovelSource;
import org.springframework.stereotype.Component;

@Component
public class NovelSourceMapper implements Mapper<NovelSource, NovelSourceDTO> {

    @Override
    public NovelSourceDTO toDTO(NovelSource entity) {
        if (entity == null) {
            return null;
        }
        
        return new NovelSourceDTO(
            entity.getId(),
            entity.getNovel() != null ? entity.getNovel().getId() : null,
            entity.getNovel() != null ? entity.getNovel().getTitle() : null,
            entity.getSourceUrl(),
            entity.getSourceId(),
            entity.getSourcePlatform(),
            entity.getEnabled(),
            entity.getLastSyncedChapter(),
            entity.getLastSyncTime(),
            entity.getSyncStatus(),
            entity.getNextSyncTime(),
            entity.getSyncIntervalMinutes(),
            entity.getErrorMessage(),
            entity.getConsecutiveFailures(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Override
    public NovelSource toEntity(NovelSourceDTO dto) {
        if (dto == null) {
            return null;
        }
        
        NovelSource entity = new NovelSource();
        entity.setId(dto.id());
        entity.setSourceUrl(dto.sourceUrl());
        entity.setSourceId(dto.sourceId());
        entity.setSourcePlatform(dto.sourcePlatform());
        entity.setEnabled(dto.enabled());
        entity.setLastSyncedChapter(dto.lastSyncedChapter());
        entity.setLastSyncTime(dto.lastSyncTime());
        entity.setSyncStatus(dto.syncStatus());
        entity.setNextSyncTime(dto.nextSyncTime());
        entity.setSyncIntervalMinutes(dto.syncIntervalMinutes());
        entity.setErrorMessage(dto.errorMessage());
        entity.setConsecutiveFailures(dto.consecutiveFailures());
        
        return entity;
    }
}
