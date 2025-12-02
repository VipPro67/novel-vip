package com.novel.vippro.DTO.Chapter;

import java.util.UUID;

public record UpdateChapterInfoDTO(
    Integer chapterNumber,
    String title,
    UUID novelId
) {}
