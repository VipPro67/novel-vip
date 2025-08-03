package com.novel.vippro.DTO.Novel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.File.FileMetadataDTO;

import lombok.Data;

@Data
public class NovelDetailDTO {
    private UUID id;
    private String title;
    private String description;
    private String author;
    private FileMetadataDTO coverImage;
    private String status;
    private List<String> categories;
    private List<String> tags;
    private List<String> genres;
    private Integer totalChapters;
    private Integer views;
    private Integer rating;
    private List<ChapterDTO> chapters;

    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}
