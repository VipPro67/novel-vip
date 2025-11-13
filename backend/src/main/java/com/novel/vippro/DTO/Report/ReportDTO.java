package com.novel.vippro.DTO.Report;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.Report.ReportStatus;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ReportDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UserDTO reporter,
    NovelDTO novel,
    ChapterDTO chapter,
    CommentDTO comment,
    String reason,
    String description,
    ReportStatus status,
    String adminResponse
) {}