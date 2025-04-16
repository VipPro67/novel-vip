package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FileMetadataDTO {
    private UUID id;
    private String fileName;
    private String contentType;
    private long size;
    private String type;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastModifiedAt;
}