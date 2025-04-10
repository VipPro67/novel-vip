package com.novel.vippro.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ChapterCreateDTO {
    private Integer chapterNumber;
    private UUID novelId;
    private String title;
    private String content;
}