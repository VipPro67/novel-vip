package com.novel.vippro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateDTO {
    @NotBlank(message = "Content is required")
    private String content;
}