package com.novel.vippro.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.models.Novel;

import java.time.LocalDateTime;
import java.util.UUID;

import org.checkerframework.checker.units.qual.C;

@Data
public class ChapterDetailDTO {
    private UUID id;
    private Integer chapterNumber;
    private String title;
    private UUID novelId;
    private String novelTitle;
    private String jsonUrl;
    private String audioUrl;

    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;

    public ChapterDetailDTO() {
    }

    public ChapterDetailDTO(Chapter c, Novel n) {
        this.id = c.getId();
        this.chapterNumber = c.getChapterNumber();
        this.title = c.getTitle();
        this.novelId = n.getId();
        this.novelTitle = n.getTitle();
        this.jsonUrl = c.getJsonUrl();
        this.audioUrl = c.getAudioUrl();
        this.createdAt = c.getCreatedAt();
        this.updatedAt = c.getUpdatedAt();
    }
}