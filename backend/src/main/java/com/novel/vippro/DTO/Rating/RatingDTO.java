package com.novel.vippro.DTO.Rating;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record RatingDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UUID userId,
    String username,
    UUID novelId,
    String novelTitle,
    Integer score,
    String review
) {}