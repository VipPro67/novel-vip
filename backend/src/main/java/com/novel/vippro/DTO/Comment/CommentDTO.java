package com.novel.vippro.DTO.Comment;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CommentDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String content,
    UUID userId,
    String username,
    UUID novelId,
    UUID chapterId,
    UUID parentId
) {}
