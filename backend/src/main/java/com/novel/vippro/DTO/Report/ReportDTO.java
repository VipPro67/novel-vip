package com.novel.vippro.DTO.Report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.Report.ReportStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class ReportDTO extends BaseDTO{
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
}