package com.novel.vippro.DTO.Report;

import com.novel.vippro.Models.Report.ReportStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportUpdateDTO {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    private String adminResponse;
}