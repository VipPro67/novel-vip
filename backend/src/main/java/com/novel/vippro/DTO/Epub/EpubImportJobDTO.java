package com.novel.vippro.DTO.Epub;

import com.novel.vippro.Models.EpubImportStatus;
import com.novel.vippro.Models.EpubImportType;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class EpubImportJobDTO {
    private UUID id;
    private EpubImportStatus status;
    private EpubImportType type;
    private UUID novelId;
    private UUID userId;
    private String slug;
    private String requestedStatus;
    private String statusMessage;
    private int totalChapters;
    private int chaptersProcessed;
    private int audioCompleted;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private String originalFileName;
}
