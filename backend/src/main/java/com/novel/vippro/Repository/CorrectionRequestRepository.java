package com.novel.vippro.Repository;

import com.novel.vippro.Models.CorrectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CorrectionRequestRepository extends JpaRepository<CorrectionRequest, UUID> {

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.status = 'PENDING' ORDER BY cr.createdAt DESC")
    Page<CorrectionRequest> findPendingCorrections(Pageable pageable);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.user.id = ?1 ORDER BY cr.createdAt DESC")
    Page<CorrectionRequest> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.novel.id = ?1 AND cr.status = 'PENDING' ORDER BY cr.createdAt DESC")
    List<CorrectionRequest> findPendingByNovelId(UUID novelId);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.chapter.id = ?1 AND cr.status = 'PENDING' ORDER BY cr.createdAt DESC")
    List<CorrectionRequest> findPendingByChapterId(UUID chapterId);

    @Query("SELECT cr FROM CorrectionRequest cr WHERE cr.status = ?1 ORDER BY cr.createdAt DESC")
    Page<CorrectionRequest> findByStatus(CorrectionRequest.CorrectionStatus status, Pageable pageable);
}
