package com.novel.vippro.DTO.ReadingHistory;

import com.novel.vippro.DTO.Novel.NovelDTO;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ReadingHistoryDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UUID userId,
    NovelDTO novel,
    int lastReadChapterIndex,
    Instant lastReadAt
) {}
