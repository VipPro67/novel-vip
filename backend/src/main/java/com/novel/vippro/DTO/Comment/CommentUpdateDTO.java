package com.novel.vippro.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CommentUpdateDTO(
    @NotBlank(message = "Content is required")
    String content
) {}