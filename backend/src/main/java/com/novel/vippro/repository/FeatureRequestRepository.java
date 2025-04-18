package com.novel.vippro.repository;

import com.novel.vippro.models.FeatureRequest;
import com.novel.vippro.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRequestRepository extends JpaRepository<FeatureRequest, Long> {
    Page<FeatureRequest> findByStatus(FeatureRequest.FeatureRequestStatus status, Pageable pageable);

    Page<FeatureRequest> findByCreatedBy(User user, Pageable pageable);

    @Query("SELECT fr FROM FeatureRequest fr WHERE fr.createdBy = :user AND fr.status = :status")
    Page<FeatureRequest> findByUserAndStatus(@Param("user") User user,
            @Param("status") FeatureRequest.FeatureRequestStatus status,
            Pageable pageable);

    @Query("SELECT fr FROM FeatureRequest fr JOIN fr.voters v WHERE v = :user")
    Page<FeatureRequest> findVotedByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(fr) > 0 FROM FeatureRequest fr JOIN fr.voters v WHERE fr.id = :featureRequestId AND v = :user")
    boolean hasUserVoted(@Param("featureRequestId") Long featureRequestId, @Param("user") User user);
}