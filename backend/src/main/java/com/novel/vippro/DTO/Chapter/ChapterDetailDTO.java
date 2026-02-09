package com.novel.vippro.DTO.Chapter;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ChapterDetailDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    Integer chapterNumber,
    String title,
    UUID novelId,
    String novelTitle,
    String jsonUrl,
    String audioUrl,
    Integer price,
    Boolean isLocked,
    Boolean isUnlocked
) {}