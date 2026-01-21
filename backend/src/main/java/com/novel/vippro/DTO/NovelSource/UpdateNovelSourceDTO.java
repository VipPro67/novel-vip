package com.novel.vippro.DTO.NovelSource;

public record UpdateNovelSourceDTO(
    Boolean enabled,
    Integer syncIntervalMinutes,
    String sourceId
) {}
