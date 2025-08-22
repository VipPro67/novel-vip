package com.novel.vippro.DTO.Chapter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadChapterResult {
    private Integer chapterNumber;
    private String title;
    private boolean success;
    private String message;
    private String fileName;
}
