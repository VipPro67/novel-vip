package com.novel.vippro.repository;

import com.novel.vippro.models.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    List<Rating> findByNovelId(UUID novelId);

    List<Rating> findByUserId(UUID userId);

    Page<Rating> findByNovelIdOrderByCreatedAtDesc(UUID novelId, Pageable pageable);

    Page<Rating> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Rating> findByUserIdAndNovelId(UUID userId, UUID novelId);

    Optional<Rating> findFirstByNovelIdOrderByCreatedAtDesc(UUID novelId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.novel.id = ?1")
    long countByNovelId(UUID novelId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.novel.id = ?1 AND r.score = ?2")
    int countByNovelIdAndScore(UUID novelId, int score);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.novel.id = ?1")
    Double getAverageRatingByNovelId(UUID novelId);

    void deleteByUserIdAndNovelId(UUID userId, UUID novelId);
}