package com.novel.vippro.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.util.UUID;

@Builder
public record CommentCreateDTO(
    @NotBlank(message = "Content is required")
    String content,

    UUID novelId,
    UUID chapterId,
    UUID parentId
) {}