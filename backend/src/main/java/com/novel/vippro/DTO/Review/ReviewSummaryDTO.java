package com.novel.vippro.DTO.Review;

import lombok.Builder;
import java.util.Map;

@Builder
public record ReviewSummaryDTO(
    double averageRating,
    long totalReviews,
    Map<Integer, Long> ratingDistribution,
    long verifiedPurchases,
    ReviewDTO latestReview,
    ReviewDTO mostHelpfulReview
) {}