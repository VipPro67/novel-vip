package com.novel.vippro.DTO.Bookmark;

import lombok.Builder;

@Builder
public record BookmarkUpdateDTO(
    String note,
    Integer progress
) {}