package com.novel.vippro.DTO.Chapter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.novel.vippro.DTO.base.BaseDTO;

import java.util.UUID;

@Data
@Getter
@Setter
public class ChapterDetailDTO extends BaseDTO {
    private UUID id;
    private Integer chapterNumber;
    private String title;
    private UUID novelId;
    private String novelTitle;
    private String jsonUrl;
    private String audioUrl;
}