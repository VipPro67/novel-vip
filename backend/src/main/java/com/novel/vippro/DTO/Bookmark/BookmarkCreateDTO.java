package com.novel.vippro.DTO.Bookmark;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.UUID;

@Builder
public record BookmarkCreateDTO(
    @NotNull(message = "Chapter ID is required")
    UUID chapterId,

    @NotNull(message = "Novel ID is required")
    UUID novelId,

    String note,

    Integer progress
) {}
