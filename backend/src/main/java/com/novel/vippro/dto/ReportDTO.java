package com.novel.vippro.dto;

import com.novel.vippro.models.Report.ReportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
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
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    @JsonProperty("resolvedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime resolvedAt;
}