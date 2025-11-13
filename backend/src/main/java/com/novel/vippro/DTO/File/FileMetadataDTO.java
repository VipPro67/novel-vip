package com.novel.vippro.DTO.File;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Builder
public record FileMetadataDTO(
    UUID id,
    String contentType,
    String publicId,
    String fileUrl,
    Instant uploadedAt,
    Instant lastModifiedAt,
    String fileName,
    String type,
    long size
) {}