package com.novel.vippro.DTO.File;

import lombok.Builder;
import java.util.UUID;

@Builder
public record FileUploadDTO(
    UUID id,
    String fileName,
    String fileUrl,
    String contentType,
    long size,
    String type
) {}