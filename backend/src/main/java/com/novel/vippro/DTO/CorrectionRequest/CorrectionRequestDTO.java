package com.novel.vippro.DTO.CorrectionRequest;

import com.google.auto.value.AutoValue.Builder;
import com.novel.vippro.Models.CorrectionRequest;

import java.time.LocalDateTime;
import java.util.UUID;
@Builder
public record CorrectionRequestDTO(
        UUID id,
        UUID userId,
        UUID novelId,
        UUID chapterId,
        Integer chapterNumber,
        Integer charIndex,
        Integer paragraphIndex,
        String originalText,
        String suggestedText,
        String reason,
        CorrectionRequest.CorrectionStatus status,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}