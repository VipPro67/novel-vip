package com.novel.vippro.DTO.System;

import com.novel.vippro.Models.EpubImportType;
import com.novel.vippro.Models.SystemJobStatus;
import com.novel.vippro.Models.SystemJobType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SystemJobDTO(
    UUID id,
    SystemJobType jobType,
    SystemJobStatus status,
    EpubImportType importType,
    UUID userId,
    UUID novelId,
    String slug,
    String requestedStatus,
    UUID chapterId,
    Integer chapterNumber,
    String statusMessage,
    int totalChapters,
    int chaptersProcessed,
    int audioCompleted,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt,
    String originalFileName
) {}
