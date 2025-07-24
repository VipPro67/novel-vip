package com.novel.vippro.DTO.Rating;

import lombok.Data;

@Data
public class RatingStatsDTO {
    private double averageRating;
    private long totalRatings;
    private int[] ratingDistribution; // Array of counts for each rating (1-5)
}