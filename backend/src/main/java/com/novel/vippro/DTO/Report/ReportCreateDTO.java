package com.novel.vippro.DTO.Report;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class ReportCreateDTO {
    private UUID novelId;
    private UUID chapterId;
    private UUID commentId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
}