package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChapterListDTO {
    private UUID id;
    private Integer chapterNumber;
    private String title;
    private UUID novelId;
    private String novelTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}