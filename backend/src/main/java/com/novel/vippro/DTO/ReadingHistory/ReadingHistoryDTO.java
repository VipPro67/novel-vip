package com.novel.vippro.DTO.ReadingHistory;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

@Data
public class ReadingHistoryDTO {
    private UUID id;
    private UUID userId;
    private UUID novelId;
    private String novelTitle;
    private String novelCover;
    private UUID chapterId;
    private String chapterTitle;
    private Integer chapterNumber;
    private Integer progress;
    private Integer readingTime;
    @JsonProperty("lastReadAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant lastReadAt;
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdAt;
}