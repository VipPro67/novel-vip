package com.novel.vippro.DTO.File;

import lombok.Builder;
import org.springframework.core.io.Resource;

@Builder
public record FileDownloadDTO(
    String fileName,
    String contentType,
    Resource resource
) {}