package com.novel.vippro.DTO.Bookmark;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

@Data
public class BookmarkDTO extends BaseDTO {
    private UUID id;
    private UUID userId;
    private UUID chapterId;
    private UUID novelId;
    private String chapterTitle;
    private String novelTitle;
    private String note;
    private Integer progress;
}
