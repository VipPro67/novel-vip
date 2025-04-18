package com.novel.vippro.services;

import com.novel.vippro.dto.ReviewCreateDTO;
import com.novel.vippro.dto.ReviewDTO;
import com.novel.vippro.dto.ReviewSummaryDTO;
import com.novel.vippro.dto.ReviewUpdateDTO;
import com.novel.vippro.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    PageResponse<ReviewDTO> getReviewsByNovel(UUID novelId, Pageable pageable);

    PageResponse<ReviewDTO> getReviewsByUser(UUID userId, Pageable pageable);

    ReviewDTO createReview(ReviewCreateDTO reviewDTO);

    ReviewDTO updateReview(UUID id, ReviewUpdateDTO reviewDTO);

    void deleteReview(UUID id);

    ReviewSummaryDTO getReviewSummary(UUID novelId);

    void voteHelpful(UUID reviewId);

    void voteUnhelpful(UUID reviewId);
}