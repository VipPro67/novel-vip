package com.novel.vippro.DTO.Novel;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String slug;
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