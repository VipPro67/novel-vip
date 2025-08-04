package com.novel.vippro.DTO.Novel;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Tag.TagDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class NovelDTO {
    private UUID id;
    private String title;
    private String description;
    private String author;
    @JsonProperty("coverImage")
    private FileMetadataDTO coverImage;
    private Set<CategoryDTO> categories;
    private Set<TagDTO> tags;
    private Set<GenreDTO> genres;
    private String status;
    private Integer totalChapters;
    private Integer views;
    private Integer rating;
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}