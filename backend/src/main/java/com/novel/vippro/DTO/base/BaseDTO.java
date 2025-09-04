package com.novel.vippro.DTO.base;

import java.util.UUID;

public class BaseDTO {
    private UUID id;
    private Boolean isActive;
    private Boolean isDeleted;
    private Long version;
    private UUID createdBy;
    private UUID updatedBy;
    private String createdAt;
    private String updatedAt;
}
