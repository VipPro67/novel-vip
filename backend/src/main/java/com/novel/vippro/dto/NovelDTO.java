package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.novel.vippro.models.Category;

@Data
public class NovelDTO {
    private UUID id;
    private String title;
    private String description;
    private String author;
    private String coverImage;
    private String status;
    private List<String> categories;
    private Integer totalChapters;
    private Integer views;
    private Integer rating;
    private List<ChapterDTO> chapters;
    private LocalDateTime updatedAt;
}