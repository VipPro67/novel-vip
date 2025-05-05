package com.novel.vippro.Services;

import com.novel.vippro.DTO.Report.ReportCreateDTO;
import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.DTO.Report.ReportUpdateDTO;
import com.novel.vippro.Exception.BadRequestException;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Comment;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Report;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public PageResponse<ReportDTO> getAllReports(Pageable pageable) {
        return new PageResponse<>(reportRepository.findAll(pageable)
                .map(this::convertToDTO));
    }

    public PageResponse<ReportDTO> getPendingReports(Pageable pageable) {
        return new PageResponse<>(
                reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.PENDING, pageable)
                        .map(this::convertToDTO));
    }

    public PageResponse<ReportDTO> getUserReports(UUID userId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO));
    }

    public PageResponse<ReportDTO> getNovelReports(UUID novelId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(this::convertToDTO));
    }

    @Transactional
    public ReportDTO createReport(ReportCreateDTO reportDTO) {
        UUID userId = userService.getCurrentUserId();
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(reportDTO.getReason());
        report.setDescription(reportDTO.getDescription());

        // Set the reported content (novel, chapter, or comment)
        if (reportDTO.getNovelId() != null) {
            Novel novel = novelRepository.findById(reportDTO.getNovelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", reportDTO.getNovelId()));
            report.setNovel(novel);

            // Check if already reported
            if (reportRepository.existsByReporterIdAndNovelIdAndStatus(
                    userId, reportDTO.getNovelId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this novel");
            }
        }

        if (reportDTO.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(reportDTO.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", reportDTO.getChapterId()));
            report.setChapter(chapter);

            if (reportRepository.existsByReporterIdAndChapterIdAndStatus(
                    userId, reportDTO.getChapterId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this chapter");
            }
        }

        if (reportDTO.getCommentId() != null) {
            Comment comment = commentRepository.findById(reportDTO.getCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", reportDTO.getCommentId()));
            report.setComment(comment);

            if (reportRepository.existsByReporterIdAndCommentIdAndStatus(
                    userId, reportDTO.getCommentId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this comment");
            }
        }

        return convertToDTO(reportRepository.save(report));
    }

    @Transactional
    public ReportDTO updateReportStatus(UUID reportId, ReportUpdateDTO updateDTO) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        report.setStatus(updateDTO.getStatus());
        report.setAdminResponse(updateDTO.getAdminResponse());
        report.setResolvedAt(LocalDateTime.now());

        return convertToDTO(reportRepository.save(report));
    }

    public ReportDTO getReport(UUID id) {
        return reportRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterUsername(report.getReporter().getUsername());

        if (report.getNovel() != null) {
            dto.setNovelId(report.getNovel().getId());
            dto.setNovelTitle(report.getNovel().getTitle());
        }

        if (report.getChapter() != null) {
            dto.setChapterId(report.getChapter().getId());
            dto.setChapterTitle(report.getChapter().getTitle());
        }

        if (report.getComment() != null) {
            dto.setCommentId(report.getComment().getId());
        }

        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setAdminResponse(report.getAdminResponse());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());

        return dto;
    }
}
