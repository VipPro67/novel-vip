package com.novel.vippro.DTO.Comment;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

@Data
public class CommentDTO extends BaseDTO {
    private UUID id;
    private String content;
    private UUID userId;
    private String username;
    private UUID novelId;
    private UUID chapterId;
    private UUID parentId;
}
