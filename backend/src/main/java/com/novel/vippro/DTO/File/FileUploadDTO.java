package com.novel.vippro.DTO.File;

import lombok.Data;
import java.util.UUID;

@Data
public class FileUploadDTO {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private long size;
    private String type;
}