package com.novel.vippro.DTO.Report;

import com.novel.vippro.Models.Report.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReportUpdateDTO(
    @NotNull(message = "Status is required")
    ReportStatus status,
    String adminResponse
) {}