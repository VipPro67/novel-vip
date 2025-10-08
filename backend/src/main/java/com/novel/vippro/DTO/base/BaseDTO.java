package com.novel.vippro.DTO.base;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Data
public class BaseDTO {
    protected UUID id;
    protected Boolean isActive;
    protected Boolean isDeleted;
    protected Long version;
    protected UUID createdBy;
    protected UUID updatedBy;
    protected Instant createdAt;
    protected Instant updatedAt;
}
