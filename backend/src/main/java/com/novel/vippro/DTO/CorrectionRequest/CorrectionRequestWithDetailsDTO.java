package com.novel.vippro.DTO.CorrectionRequest;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.CorrectionRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record CorrectionRequestWithDetailsDTO(
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
        String previousParagraph,
        String paragraphText,
        String nextParagraph,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserDTO user,
        NovelDTO novel,
        ChapterDTO chapter
) {
}