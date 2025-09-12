package com.novel.vippro.DTO.base;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Data
public class BaseDTO {
    private UUID id;
    private Boolean isActive;
    private Boolean isDeleted;
    private Long version;
    private UUID createdBy;
    private UUID updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
