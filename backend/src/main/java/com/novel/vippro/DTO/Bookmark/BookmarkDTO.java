package com.novel.vippro.DTO.Bookmark;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record BookmarkDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UUID userId,
    UUID chapterId,
    UUID novelId,
    String chapterTitle,
    String novelTitle,
    String note,
    Integer progress
) {}
