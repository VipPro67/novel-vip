package com.novel.vippro.DTO.CorrectionRequest;

import java.util.List;
import java.util.UUID;

public record CreateCorrectionRequestDTO(
    UUID novelId,
    UUID chapterId,
    Integer chapterNumber,
    Integer charIndex,
    Integer paragraphIndex,
    List<Integer> paragraphIndices,  // For multi-paragraph selection
    String originalText,
    String suggestedText,
    String reason
) {
}