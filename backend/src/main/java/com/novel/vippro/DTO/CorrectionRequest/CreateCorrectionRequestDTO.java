package com.novel.vippro.DTO.CorrectionRequest;

import java.util.UUID;

public record CreateCorrectionRequestDTO(
    UUID novelId,
    UUID chapterId,
    Integer chapterNumber,
    Integer charIndex,
    Integer paragraphIndex,
    String originalText,
    String suggestedText,
    String reason
) {
}