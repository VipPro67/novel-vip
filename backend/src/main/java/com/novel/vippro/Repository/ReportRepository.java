package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Report;
import com.novel.vippro.Models.Report.ReportStatus;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<Report> findByReporterIdOrderByCreatedAtDesc(UUID reporterId, Pageable pageable);

    Page<Report> findByNovelIdOrderByCreatedAtDesc(UUID novelId, Pageable pageable);

    Page<Report> findByChapterIdOrderByCreatedAtDesc(UUID chapterId, Pageable pageable);

    Page<Report> findByCommentIdOrderByCreatedAtDesc(UUID commentId, Pageable pageable);

    boolean existsByReporterIdAndNovelIdAndStatus(UUID reporterId, UUID novelId, ReportStatus status);

    boolean existsByReporterIdAndChapterIdAndStatus(UUID reporterId, UUID chapterId, ReportStatus status);

    boolean existsByReporterIdAndCommentIdAndStatus(UUID reporterId, UUID commentId, ReportStatus status);
}