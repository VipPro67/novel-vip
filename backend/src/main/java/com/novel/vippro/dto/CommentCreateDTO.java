package com.novel.vippro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CommentCreateDTO {
    @NotBlank(message = "Content is required")
    private String content;

    private UUID novelId;
    private UUID chapterId;
}