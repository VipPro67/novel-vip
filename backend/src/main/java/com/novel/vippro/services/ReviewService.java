package com.novel.vippro.services;

import com.novel.vippro.dto.ReviewCreateDTO;
import com.novel.vippro.dto.ReviewDTO;
import com.novel.vippro.dto.ReviewSummaryDTO;
import com.novel.vippro.dto.ReviewUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    Page<ReviewDTO> getReviewsByNovel(UUID novelId, Pageable pageable);

    Page<ReviewDTO> getReviewsByUser(UUID userId, Pageable pageable);

    ReviewDTO createReview(ReviewCreateDTO reviewDTO);

    ReviewDTO updateReview(UUID id, ReviewUpdateDTO reviewDTO);

    void deleteReview(UUID id);

    ReviewSummaryDTO getReviewSummary(UUID novelId);

    void voteHelpful(UUID reviewId);

    void voteUnhelpful(UUID reviewId);
}