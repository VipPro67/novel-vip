package com.novel.vippro.DTO.Chapter;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Models.Novel;

import java.time.LocalDateTime;
import java.util.UUID;

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
        this.jsonUrl = c.getJsonFile() != null ? c.getJsonFile().getFileUrl() : null;
        this.audioUrl = c.getAudioFile() != null ? c.getAudioFile().getFileUrl() : null;
        this.createdAt = c.getCreatedAt();
        this.updatedAt = c.getUpdatedAt();
    }
}