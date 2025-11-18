package com.novel.vippro.Services;

import com.novel.vippro.DTO.Report.ReportCreateDTO;
import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.DTO.Report.ReportUpdateDTO;
import com.novel.vippro.Exception.BadRequestException;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Comment;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Report;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.*;
import com.novel.vippro.Security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    private Mapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> getAllReports(Pageable pageable) {
        return new PageResponse<>(reportRepository.findAll(pageable)
                .map(mapper::ReporttoDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> getPendingReports(Pageable pageable) {
        return new PageResponse<>(
                reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.PENDING, pageable)
                        .map(mapper::ReporttoDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> getUserReports(UUID userId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::ReporttoDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> getNovelReports(UUID novelId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(mapper::ReporttoDTO));
    }

    @Transactional
    public ReportDTO createReport(ReportCreateDTO reportDTO) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(reportDTO.reason());
        report.setDescription(reportDTO.description());

        // Set the reported content (novel, chapter, or comment)
        if (reportDTO.novelId() != null) {
            Novel novel = novelRepository.findById(reportDTO.novelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", reportDTO.novelId()));
            report.setNovel(novel);

            // Check if already reported
            if (reportRepository.existsByReporterIdAndNovelIdAndStatus(
                    userId, reportDTO.novelId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this novel");
            }
        }

        if (reportDTO.chapterId() != null) {
            Chapter chapter = chapterRepository.findById(reportDTO.chapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", reportDTO.chapterId()));
            report.setChapter(chapter);

            if (reportRepository.existsByReporterIdAndChapterIdAndStatus(
                    userId, reportDTO.chapterId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this chapter");
            }
        }

        if (reportDTO.commentId() != null) {
            Comment comment = commentRepository.findById(reportDTO.commentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", reportDTO.commentId()));
            report.setComment(comment);

            if (reportRepository.existsByReporterIdAndCommentIdAndStatus(
                    userId, reportDTO.commentId(), Report.ReportStatus.PENDING)) {
                throw new BadRequestException("You have already reported this comment");
            }
        }

        return mapper.ReporttoDTO(reportRepository.save(report));
    }

    @Transactional
    public ReportDTO updateReportStatus(UUID reportId, ReportUpdateDTO updateDTO) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        report.setStatus(updateDTO.status());
        report.setAdminResponse(updateDTO.adminResponse());
        report.setResolvedAt(Instant.now());

        return mapper.ReporttoDTO(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ReportDTO getReport(UUID id) {
        return reportRepository.findById(id)
                .map(mapper::ReporttoDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDTO> getMyReport(Pageable pageable) {        
        UUID userId = UserDetailsImpl.getCurrentUserId();
        return new PageResponse<>(reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::ReporttoDTO));

    }
}
