package com.novel.vippro.Utils;

import lombok.Data;

@Data
public class EpubChapterDTO {
    private int chapterNumber;
    private String title;
    private String contentHtml;
}
