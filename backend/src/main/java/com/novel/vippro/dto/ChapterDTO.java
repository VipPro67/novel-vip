package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChapterDTO {
    private Long id;
    private String title;
    private Integer chapterNumber;
    private LocalDateTime updatedAt;
    private Integer views;
}