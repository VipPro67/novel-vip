package com.novel.vippro.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class NovelDTO {
    private UUID id;
    private String title;
    private String description;
    private String author;
    private String coverImage;
    private String status;
    private List<String> categories;
    private Integer totalChapters;
    private Integer views;
    private Integer rating;
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}