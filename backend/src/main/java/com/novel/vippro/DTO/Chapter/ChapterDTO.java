package com.novel.vippro.DTO.Chapter;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ChapterDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String title,
    Integer chapterNumber,
    UUID novelId,
    String novelTitle,
    Integer views,
    Integer price,
    Boolean isLocked,
    Boolean isUnlocked
) {}