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
    private UserService userService;

    @Autowired
    private Mapper mapper;

    public PageResponse<ReportDTO> getAllReports(Pageable pageable) {
        return new PageResponse<>(reportRepository.findAll(pageable)
                .map(mapper::ReporttoDTO));
    }

    public PageResponse<ReportDTO> getPendingReports(Pageable pageable) {
        return new PageResponse<>(
                reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.PENDING, pageable)
                        .map(mapper::ReporttoDTO));
    }

    public PageResponse<ReportDTO> getUserReports(UUID userId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::ReporttoDTO));
    }

    public PageResponse<ReportDTO> getNovelReports(UUID novelId, Pageable pageable) {
        return new PageResponse<>(reportRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(mapper::ReporttoDTO));
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

        return mapper.ReporttoDTO(reportRepository.save(report));
    }

    @Transactional
    public ReportDTO updateReportStatus(UUID reportId, ReportUpdateDTO updateDTO) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        report.setStatus(updateDTO.getStatus());
        report.setAdminResponse(updateDTO.getAdminResponse());
        report.setResolvedAt(Instant.now());

        return mapper.ReporttoDTO(reportRepository.save(report));
    }

    public ReportDTO getReport(UUID id) {
        return reportRepository.findById(id)
                .map(mapper::ReporttoDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }
}
