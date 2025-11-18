package com.novel.vippro.DTO.Chapter;

import lombok.Builder;
import java.util.UUID;

@Builder
public record CreateChapterDTO(
    Integer chapterNumber,
    UUID novelId,
    String title,
    String contentType,
    String content,
    String contentHtml,
    ContentFormat format
) {
    public enum ContentFormat { TEXT , HTML }
}
