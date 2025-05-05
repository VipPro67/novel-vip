package com.novel.vippro.DTO.Review;

import lombok.Data;
import java.util.Map;

@Data
public class ReviewSummaryDTO {
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingDistribution;
    private long verifiedPurchases;
    private ReviewDTO latestReview;
    private ReviewDTO mostHelpfulReview;
}