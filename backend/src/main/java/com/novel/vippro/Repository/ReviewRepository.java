package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Review;
import com.novel.vippro.Models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByNovel(Novel novel, Pageable pageable);

    Page<Review> findByUser(User user, Pageable pageable);

    Optional<Review> findByNovelAndUser(Novel novel, User user);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.novel = :novel")
    long countByNovel(@Param("novel") Novel novel);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.novel = :novel")
    Optional<Double> calculateAverageRating(@Param("novel") Novel novel);

    @Query("SELECT r.rating as rating, COUNT(r) as count FROM Review r WHERE r.novel = :novel GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("novel") Novel novel);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.novel = :novel AND r.isVerifiedPurchase = true")
    long countVerifiedPurchases(@Param("novel") Novel novel);

    @Query("SELECT r FROM Review r WHERE r.novel = :novel ORDER BY r.helpfulVotes DESC")
    List<Review> findMostHelpfulReviews(@Param("novel") Novel novel, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.novel = :novel ORDER BY r.createdAt DESC")
    List<Review> findLatestReviews(@Param("novel") Novel novel, Pageable pageable);
}