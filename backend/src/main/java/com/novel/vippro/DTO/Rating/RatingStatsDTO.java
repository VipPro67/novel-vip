package com.novel.vippro.DTO.Rating;

import lombok.Builder;

@Builder
public record RatingStatsDTO(
    double averageRating,
    long totalRatings,
    int[] ratingDistribution
) {}