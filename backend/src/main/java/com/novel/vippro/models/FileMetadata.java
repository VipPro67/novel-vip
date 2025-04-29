package com.novel.vippro.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "file_metadata")
public class FileMetadata {
    @Id
    @GeneratedValue(generator = "UUID")
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