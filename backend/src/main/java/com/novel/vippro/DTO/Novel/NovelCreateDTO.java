package com.novel.vippro.DTO.Novel;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Builder
public record NovelCreateDTO(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,

    @NotBlank(message = "Slug is required")
    @Size(min = 1, max = 255, message = "Slug must be between 1 and 255 characters")
    String slug,

    String description,

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
    String author,

    @NotBlank(message = "Status is required")
    String status,

    List<String> categories,

    List<String> genres,

    List<String> tags
) {}