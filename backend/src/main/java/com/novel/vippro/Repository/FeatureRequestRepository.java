package com.novel.vippro.Repository;

import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.Models.FeatureRequest;
import com.novel.vippro.Models.User;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRequestRepository extends JpaRepository<FeatureRequest, UUID> {
        @Query("SELECT fr, u FROM FeatureRequest fr JOIN fr.createdBy u WHERE fr.status = :status")
        Page<FeatureRequestDTO> findByStatus(@Param("status") FeatureRequest.FeatureRequestStatus status,
                        Pageable pageable);

        @Query("SELECT fr FROM FeatureRequest fr WHERE fr.createdBy = :user")
        Page<FeatureRequest> findByCreatedBy(User user, Pageable pageable);

        @Query("SELECT fr FROM FeatureRequest fr WHERE fr.createdBy = :user AND fr.status = :status")
        Page<FeatureRequest> findByUserAndStatus(@Param("user") User user,
                        @Param("status") FeatureRequest.FeatureRequestStatus status,
                        Pageable pageable);

        @Query("SELECT fr FROM FeatureRequest fr JOIN fr.voters v WHERE v = :user")
        Page<FeatureRequest> findVotedByUser(@Param("user") User user, Pageable pageable);

        @Query("SELECT COUNT(fr) > 0 FROM FeatureRequest fr JOIN fr.voters v WHERE fr.id = :featureRequestId AND v = :user")
        boolean hasUserVoted(@Param("featureRequestId") UUID featureRequestId, @Param("user") User user);
}