package com.novel.vippro.Repository;

import com.novel.vippro.Models.CorrectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CorrectionRequestRepository extends JpaRepository<CorrectionRequest, UUID> {
    Page<CorrectionRequest> findByStatus(CorrectionRequest.CorrectionStatus status, Pageable pageable);
    Page<CorrectionRequest> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.status = 'PENDING'")
    Page<CorrectionRequest> findPendingCorrections(Pageable pageable);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.novel.id = :novelId AND cr.status = 'PENDING'")
    List<CorrectionRequest> findPendingByNovelId(@Param("novelId") UUID novelId);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.chapter.id = :chapterId AND cr.status = 'PENDING'")
    List<CorrectionRequest> findPendingByChapterId(@Param("chapterId") UUID chapterId);
}