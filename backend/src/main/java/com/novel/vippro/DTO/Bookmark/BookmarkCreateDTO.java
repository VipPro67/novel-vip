package com.novel.vippro.DTO.Bookmark;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class BookmarkCreateDTO {
    @NotNull(message = "Chapter ID is required")
    private UUID chapterId;

    @NotNull(message = "Novel ID is required")
    private UUID novelId;

    private String note;

    private Integer progress;
}
