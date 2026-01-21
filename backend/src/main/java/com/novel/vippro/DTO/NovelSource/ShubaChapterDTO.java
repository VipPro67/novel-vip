package com.novel.vippro.DTO.NovelSource;

import lombok.Data;

@Data
public class ShubaChapterDTO {
    private int chapterNumber;
    private String title;
    private String contentHtml;
    private String sourceChapterId;
}
