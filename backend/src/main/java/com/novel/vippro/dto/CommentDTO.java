package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentDTO {
    private UUID id;
    private String content;
    private UUID userId;
    private String username;
    private UUID novelId;
    private UUID chapterId;
    private UUID parentId;
    private List<CommentDTO> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}