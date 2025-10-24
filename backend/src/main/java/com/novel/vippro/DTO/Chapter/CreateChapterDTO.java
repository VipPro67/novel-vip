package com.novel.vippro.DTO.Chapter;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateChapterDTO {
    public enum ContentFormat { TEXT , HTML }

    private Integer chapterNumber;
    private UUID novelId;
    private String title;
    private String contentType;
    private String content;
    private String contentHtml;
    private ContentFormat format;   
}
