package com.novel.vippro.DTO.Rating;

import lombok.Builder;
import java.util.Map;

@Builder
public record RatingSummaryDTO(
    double averageRating,
    long totalRatings,
    Map<Integer, Long> distribution,
    RatingDTO latestRating,
    RatingDTO userRating
) {}