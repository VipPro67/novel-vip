package com.novel.vippro.dto;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class FileDownloadDTO {
    private String fileName;
    private String contentType;
    private Resource resource;
}