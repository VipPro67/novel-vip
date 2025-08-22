package com.novel.vippro.DTO.Novel;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Data
public class NovelCreateDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(min = 1, max = 255, message = "Slug must be between 1 and 255 characters")
    private String slug;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
    private String author;

    @NotBlank(message = "Cover image URL is required")
    private MultipartFile coverImage;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Categories list cannot be null")
    private List<String> categories;

    private List<String> genres;

    private List<String> tags;
}