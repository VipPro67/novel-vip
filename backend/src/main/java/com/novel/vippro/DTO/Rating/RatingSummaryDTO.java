package com.novel.vippro.DTO.Rating;

import lombok.Data;
import java.util.Map;

@Data
public class RatingSummaryDTO {
    private double averageRating;
    private long totalRatings;
    private Map<Integer, Long> distribution; // Rating value -> Count
    private RatingDTO latestRating;
    private RatingDTO userRating; // Current user's rating if exists
}