package com.novel.vippro.DTO.FeatureRequest;

import com.novel.vippro.Models.FeatureRequest;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record FeatureRequestDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String title,
    String description,
    UUID userId,
    String username,
    String fullName,
    FeatureRequest.FeatureRequestStatus status,
    Integer voteCount
) {}