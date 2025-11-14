package com.novel.vippro.DTO.Group;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record GroupDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String name,
    String description
) {}