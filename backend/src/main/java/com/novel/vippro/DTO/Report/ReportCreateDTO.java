package com.novel.vippro.DTO.Report;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.util.UUID;

@Builder
public record ReportCreateDTO(
    UUID novelId,
    UUID chapterId,
    UUID commentId,
    @NotBlank(message = "Reason is required")
    String reason,
    String description
) {}