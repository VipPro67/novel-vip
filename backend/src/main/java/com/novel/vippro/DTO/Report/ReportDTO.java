package com.novel.vippro.DTO.Report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.novel.vippro.Models.Report.ReportStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
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
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdAt;
    @JsonProperty("resolvedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant resolvedAt;
}