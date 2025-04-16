package com.novel.vippro.services;

import com.novel.vippro.dto.RatingDTO;
import com.novel.vippro.dto.RatingCreateDTO;
import com.novel.vippro.dto.RatingUpdateDTO;
import com.novel.vippro.dto.RatingStatsDTO;
import com.novel.vippro.dto.RatingSummaryDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.exception.BadRequestException;
import com.novel.vippro.models.Rating;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.RatingRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public Page<RatingDTO> getNovelRatings(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return ratingRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public RatingDTO rateNovel(UUID novelId, RatingCreateDTO ratingDTO) {
        UUID userId = userService.getCurrentUserId();

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Rating rating = ratingRepository.findByUserIdAndNovelId(userId, novelId)
                .orElse(new Rating());

        rating.setUser(user);
        rating.setNovel(novel);
        rating.setScore(ratingDTO.getScore());
        rating.setReview(ratingDTO.getReview());

        Rating savedRating = ratingRepository.save(rating);
        updateNovelRating(novel);

        return convertToDTO(savedRating);
    }

    public RatingDTO getUserRating(UUID novelId) {
        UUID userId = userService.getCurrentUserId();
        return ratingRepository.findByUserIdAndNovelId(userId, novelId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
    }

    @Transactional
    public void deleteRating(UUID novelId) {
        UUID userId = userService.getCurrentUserId();

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        if (!ratingRepository.findByUserIdAndNovelId(userId, novelId).isPresent()) {
            throw new ResourceNotFoundException("Rating not found");
        }

        ratingRepository.deleteByUserIdAndNovelId(userId, novelId);
        updateNovelRating(novel);
    }

    public RatingStatsDTO getRatingStats(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }

        RatingStatsDTO stats = new RatingStatsDTO();
        stats.setTotalRatings(ratingRepository.countByNovelId(novelId));
        stats.setAverageRating(ratingRepository.getAverageRatingByNovelId(novelId) != null
                ? ratingRepository.getAverageRatingByNovelId(novelId)
                : 0.0);

        int[] distribution = new int[5];
        for (int i = 1; i <= 5; i++) {
            distribution[i - 1] = ratingRepository.countByNovelIdAndScore(novelId, i);
        }
        stats.setRatingDistribution(distribution);

        return stats;
    }

    @Transactional
    protected void updateNovelRating(Novel novel) {
        Double averageRating = ratingRepository.getAverageRatingByNovelId(novel.getId());
        novel.setRating(averageRating != null ? (int) Math.round(averageRating) : 0);
        novelRepository.save(novel);
    }

    private RatingDTO convertToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUser().getId());
        dto.setUsername(rating.getUser().getUsername());
        dto.setNovelId(rating.getNovel().getId());
        dto.setNovelTitle(rating.getNovel().getTitle());
        dto.setScore(rating.getScore());
        dto.setReview(rating.getReview());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        return dto;
    }

    @Transactional
    public RatingDTO updateRating(UUID id, RatingUpdateDTO ratingDTO) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "id", id));

        // Verify ownership
        UUID currentUserId = userService.getCurrentUserId();
        if (!rating.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Not authorized to update this rating");
        }

        rating.setScore(ratingDTO.getScore());
        rating.setReview(ratingDTO.getReview());

        Rating updatedRating = ratingRepository.save(rating);
        updateNovelRating(rating.getNovel());

        return convertToDTO(updatedRating);
    }

    public Page<RatingDTO> getUserRatings(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return ratingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    public RatingDTO getUserNovelRating(UUID novelId, UUID userId) {
        return ratingRepository.findByUserIdAndNovelId(userId, novelId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
    }

    public Map<Integer, Long> getRatingDistribution(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, (long) ratingRepository.countByNovelIdAndScore(novelId, i));
        }
        return distribution;
    }

    public RatingSummaryDTO getRatingSummary(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }

        RatingSummaryDTO summary = new RatingSummaryDTO();
        summary.setAverageRating(ratingRepository.getAverageRatingByNovelId(novelId) != null
                ? ratingRepository.getAverageRatingByNovelId(novelId)
                : 0.0);
        summary.setTotalRatings(ratingRepository.countByNovelId(novelId));
        summary.setDistribution(getRatingDistribution(novelId));

        // Get latest rating
        ratingRepository.findFirstByNovelIdOrderByCreatedAtDesc(novelId)
                .ifPresent(rating -> summary.setLatestRating(convertToDTO(rating)));

        // Get current user's rating if authenticated
        try {
            UUID currentUserId = userService.getCurrentUserId();
            ratingRepository.findByUserIdAndNovelId(currentUserId, novelId)
                    .ifPresent(rating -> summary.setUserRating(convertToDTO(rating)));
        } catch (Exception e) {
            // User not authenticated, skip user rating
        }

        return summary;
    }

    public RatingStatsDTO getNovelRatingStats(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }

        RatingStatsDTO stats = new RatingStatsDTO();
        stats.setAverageRating(ratingRepository.getAverageRatingByNovelId(novelId) != null
                ? ratingRepository.getAverageRatingByNovelId(novelId)
                : 0.0);
        stats.setTotalRatings(ratingRepository.countByNovelId(novelId));

        return stats;
    }
}