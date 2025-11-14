package com.novel.vippro.DTO.Review;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ReviewDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UUID novelId,
    String novelTitle,
    UUID userId,
    String username,
    String userAvatar,
    String title,
    String content,
    int rating,
    boolean isVerifiedPurchase,
    int helpfulVotes,
    int unhelpfulVotes,
    boolean isEdited
) {}