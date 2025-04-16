package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReadingHistoryDTO {
    private UUID id;
    private UUID userId;
    private UUID novelId;
    private String novelTitle;
    private String novelCover;
    private UUID chapterId;
    private String chapterTitle;
    private Integer chapterNumber;
    private Integer progress;
    private Integer readingTime;
    private LocalDateTime lastReadAt;
    private LocalDateTime createdAt;
}