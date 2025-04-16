package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookmarkDTO {
    private UUID id;
    private UUID userId;
    private UUID chapterId;
    private UUID novelId;
    private String chapterTitle;
    private String novelTitle;
    private String note;
    private Integer progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}