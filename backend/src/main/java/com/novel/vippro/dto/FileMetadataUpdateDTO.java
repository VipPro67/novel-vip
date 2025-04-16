package com.novel.vippro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileMetadataUpdateDTO {
    @NotBlank(message = "File name is required")
    private String fileName;
    private String type;
}