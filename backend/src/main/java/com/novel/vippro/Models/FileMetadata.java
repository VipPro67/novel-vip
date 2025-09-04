package com.novel.vippro.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
<<<<<<< Updated upstream

import java.time.Instant;
import java.util.UUID;
=======
>>>>>>> Stashed changes
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.novel.vippro.Models.base.BaseEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Data
@Entity
@Table(name = "file_metadata")
public class FileMetadata extends BaseEntity {
    private String contentType;
    private String publicId;
    private String fileUrl;
<<<<<<< Updated upstream
    private Instant uploadedAt = Instant.now();
    private Instant lastModifiedAt = Instant.now();
=======
>>>>>>> Stashed changes
    private String fileName;
    private String type;
    private long size;
}