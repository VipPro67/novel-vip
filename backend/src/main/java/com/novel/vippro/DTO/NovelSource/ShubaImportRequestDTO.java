package com.novel.vippro.DTO.NovelSource;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShubaImportRequestDTO(
    @NotNull(message = "Novel source ID is required")
    UUID novelSourceId,
    
    Integer startChapter,
    
    Integer endChapter,
    
    Boolean fullImport
) {}
