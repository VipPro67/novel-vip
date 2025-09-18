package com.novel.vippro.DTO.File;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Data
public class FileMetadataDTO {
    private UUID id;
    private String contentType;
    private String publicId;
    private String fileUrl;
    private Instant uploadedAt = Instant.now();
    private Instant lastModifiedAt = Instant.now();
    private String fileName;
    private String type;
    private long size;
}