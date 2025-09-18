package com.novel.vippro.Models;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.novel.vippro.DTO.File.FileMetadataDTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Document(indexName = "novels")  // ES index name
public class NovelDocument {

    @Id
    private UUID id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String slug;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Boolean)
    private boolean isPublic;

    @Field(type = FieldType.Integer)
    private Integer totalChapters;

    @Field(type = FieldType.Integer)
    private Integer views;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Object)
    private FileMetadataDTO coverImage;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant updatedAt;
}
