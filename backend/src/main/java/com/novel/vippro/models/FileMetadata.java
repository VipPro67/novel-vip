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

    private String fileName;
    private String contentType;
    private long size;
    private String type;
    private String publicId;
    private String fileUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastModifiedAt;
}