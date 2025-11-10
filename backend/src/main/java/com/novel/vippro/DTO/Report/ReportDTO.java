package com.novel.vippro.DTO.Report;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.Report.ReportStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class ReportDTO extends BaseDTO{
    private UUID id;
    private UserDTO reporter;
    private NovelDTO novel;
    private ChapterDTO chapter;
    private CommentDTO comment;
    private String reason;
    private String description;
    private ReportStatus status;
    private String adminResponse;
}