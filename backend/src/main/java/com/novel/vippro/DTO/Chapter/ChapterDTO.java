package com.novel.vippro.DTO.Chapter;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

@Data
public class ChapterDTO extends BaseDTO {
    private UUID id;
    private String title;
    private Integer chapterNumber;
    private UUID novelId;
    private String novelTitle;
    private Integer views;
}