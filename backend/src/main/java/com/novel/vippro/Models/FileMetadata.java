package com.novel.vippro.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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
    private Instant uploadedAt = Instant.now();
    private Instant lastModifiedAt = Instant.now();
    private String fileName;
    private String type;
    private long size;
}