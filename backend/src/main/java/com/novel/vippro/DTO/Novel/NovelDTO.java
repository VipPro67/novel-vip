package com.novel.vippro.DTO.Novel;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.DTO.base.BaseDTO;

import java.util.Set;

@Data
public class NovelDTO extends BaseDTO {
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
}