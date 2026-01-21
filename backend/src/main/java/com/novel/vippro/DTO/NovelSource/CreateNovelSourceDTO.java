package com.novel.vippro.DTO.NovelSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateNovelSourceDTO(
    @NotNull(message = "Novel ID is required")
    UUID novelId,
    
    @NotBlank(message = "Source URL is required")
    String sourceUrl,
    
    String sourceId,
    
    String sourcePlatform,
    
    Integer syncIntervalMinutes
) {}
