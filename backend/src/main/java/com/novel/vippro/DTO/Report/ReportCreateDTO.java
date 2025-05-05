package com.novel.vippro.DTO.Report;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class ReportCreateDTO {
    private UUID novelId;
    private UUID chapterId;
    private UUID commentId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
}