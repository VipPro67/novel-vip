package com.novel.vippro.DTO.System;

import com.novel.vippro.Models.EpubImportType;
import com.novel.vippro.Models.SystemJobStatus;
import com.novel.vippro.Models.SystemJobType;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class SystemJobDTO {
    private UUID id;
    private SystemJobType jobType;
    private SystemJobStatus status;
    private EpubImportType importType;
    private UUID userId;
    private UUID novelId;
    private String slug;
    private String requestedStatus;
    private UUID chapterId;
    private Integer chapterNumber;
    private String statusMessage;
    private int totalChapters;
    private int chaptersProcessed;
    private int audioCompleted;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private String originalFileName;
}
