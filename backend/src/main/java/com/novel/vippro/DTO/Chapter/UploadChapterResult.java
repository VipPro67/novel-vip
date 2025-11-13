package com.novel.vippro.DTO.Chapter;

import lombok.Builder;

@Builder
public record UploadChapterResult(
    Integer chapterNumber,
    String title,
    boolean success,
    String message,
    String fileName
) {}
