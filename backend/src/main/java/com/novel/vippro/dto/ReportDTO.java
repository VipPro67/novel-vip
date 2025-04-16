package com.novel.vippro.dto;

import com.novel.vippro.models.Report.ReportStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportDTO {
    private UUID id;
    private UUID reporterId;
    private String reporterUsername;
    private UUID novelId;
    private String novelTitle;
    private UUID chapterId;
    private String chapterTitle;
    private UUID commentId;
    private String reason;
    private String description;
    private ReportStatus status;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}