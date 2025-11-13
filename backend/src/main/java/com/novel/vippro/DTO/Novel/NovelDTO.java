package com.novel.vippro.DTO.Novel;

import lombok.Builder;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Tag.TagDTO;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record NovelDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String title,
    String description,
    String author,
    String slug,
    String imageUrl,
    Set<CategoryDTO> categories,
    Set<TagDTO> tags,
    Set<GenreDTO> genres,
    String status,
    Integer totalChapters,
    Integer totalViews,
    Integer monthlyViews,
    Integer dailyViews,
    Integer rating
) {}