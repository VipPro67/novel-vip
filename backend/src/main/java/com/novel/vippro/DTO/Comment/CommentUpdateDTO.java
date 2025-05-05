package com.novel.vippro.DTO.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateDTO {
    @NotBlank(message = "Content is required")
    private String content;
}