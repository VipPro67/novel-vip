package com.novel.vippro.DTO.File;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileMetadataUpdateDTO {
    @NotBlank(message = "File name is required")
    private String fileName;
    private String type;
}