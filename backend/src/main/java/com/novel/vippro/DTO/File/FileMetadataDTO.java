package com.novel.vippro.DTO.File;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Data
public class FileMetadataDTO {
    private UUID id;
    private String contentType;
    private String publicId;
    private String fileUrl;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    private LocalDateTime lastModifiedAt = LocalDateTime.now();
    private String fileName;
    private String type;
    private long size;
}