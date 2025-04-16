package com.novel.vippro.dto;

import com.novel.vippro.models.Report.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportUpdateDTO {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    private String adminResponse;
}