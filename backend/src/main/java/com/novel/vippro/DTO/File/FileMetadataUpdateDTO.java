package com.novel.vippro.DTO.File;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FileMetadataUpdateDTO(
    @NotBlank(message = "File name is required")
    String fileName,
    String type
) {}