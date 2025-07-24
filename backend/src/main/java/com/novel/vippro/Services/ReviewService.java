package com.novel.vippro.Services;

import com.novel.vippro.DTO.Review.ReviewCreateDTO;
import com.novel.vippro.DTO.Review.ReviewDTO;
import com.novel.vippro.DTO.Review.ReviewSummaryDTO;
import com.novel.vippro.DTO.Review.ReviewUpdateDTO;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.Review;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.ReviewRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Services.ReviewService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(value = "reviews", key = "#novelId + '_' + #pageable")
    public PageResponse<ReviewDTO> getReviewsByNovel(UUID novelId, Pageable pageable) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Novel not found"));
        return new PageResponse<>(reviewRepository.findByNovel(novel, pageable).map(this::toDTO));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "reviewsByUser", key = "#userId + '_' + #pageable")
    public PageResponse<ReviewDTO> getReviewsByUser(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new PageResponse<>(reviewRepository.findByUser(user, pageable).map(this::toDTO));
    }

    @Transactional
    public ReviewDTO createReview(ReviewCreateDTO reviewDTO) {
        User currentUser = userService.getCurrentUser();
        Novel novel = novelRepository.findById(reviewDTO.getNovelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Novel not found"));

        reviewRepository.findByNovelAndUser(novel, currentUser).ifPresent(review -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already reviewed this novel");
        });

        Review review = new Review();
        review.setNovel(novel);
        review.setUser(currentUser);
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setRating(reviewDTO.getRating());
        review.setVerifiedPurchase(false);

        return toDTO(reviewRepository.save(review));
    }

    @Transactional
    @Cacheable(value = "review", key = "#id")
    public ReviewDTO updateReview(UUID id, ReviewUpdateDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        User currentUser = userService.getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this review");
        }

        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setRating(reviewDTO.getRating());
        review.setEdited(true);

        return toDTO(reviewRepository.save(review));
    }

    @Transactional
    @Cacheable(value = "review", key = "#id")
    public void deleteReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        User currentUser = userService.getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "reviewSummary", key = "#novelId")
    public ReviewSummaryDTO getReviewSummary(UUID novelId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Novel not found"));

        ReviewSummaryDTO summary = new ReviewSummaryDTO();
        summary.setAverageRating(reviewRepository.calculateAverageRating(novel).orElse(0.0));
        summary.setTotalReviews(reviewRepository.countByNovel(novel));
        summary.setVerifiedPurchases(reviewRepository.countVerifiedPurchases(novel));

        // Get rating distribution
        List<Object[]> distribution = reviewRepository.getRatingDistribution(novel);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (Object[] result : distribution) {
            ratingDistribution.put((Integer) result[0], (Long) result[1]);
        }
        summary.setRatingDistribution(ratingDistribution);

        // Get latest review
        List<Review> latestReviews = reviewRepository.findLatestReviews(novel, PageRequest.of(0, 1));
        if (!latestReviews.isEmpty()) {
            summary.setLatestReview(toDTO(latestReviews.get(0)));
        }

        // Get most helpful review
        List<Review> helpfulReviews = reviewRepository.findMostHelpfulReviews(novel, PageRequest.of(0, 1));
        if (!helpfulReviews.isEmpty()) {
            summary.setMostHelpfulReview(toDTO(helpfulReviews.get(0)));
        }

        return summary;
    }

    @Transactional
    public void voteHelpful(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void voteUnhelpful(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setUnhelpfulVotes(review.getUnhelpfulVotes() + 1);
        reviewRepository.save(review);
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setNovelId(review.getNovel().getId());
        dto.setNovelTitle(review.getNovel().getTitle());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setUserAvatar(review.getUser().getAvatar());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setVerifiedPurchase(review.isVerifiedPurchase());
        dto.setHelpfulVotes(review.getHelpfulVotes());
        dto.setUnhelpfulVotes(review.getUnhelpfulVotes());
        dto.setEdited(review.isEdited());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}