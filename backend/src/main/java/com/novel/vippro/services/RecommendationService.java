package com.novel.vippro.services;

import com.novel.vippro.models.Genre;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Rating;
import com.novel.vippro.models.Tag;
import com.novel.vippro.models.User;
import com.novel.vippro.models.UserPreferences;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.repository.RatingRepository;
import com.novel.vippro.repository.UserPreferencesRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
        private final NovelRepository novelRepository;
        private final RatingRepository ratingRepository;
        private final UserPreferencesRepository userPreferencesRepository;
        private final UserService userService;

        @Transactional(readOnly = true)
        public PageResponse<Novel> getPersonalizedRecommendations(Pageable pageable) {
                User currentUser = userService.getCurrentUser();
                UserPreferences preferences = userPreferencesRepository.findByUser(currentUser)
                                .orElseGet(() -> createDefaultPreferences(currentUser));

                // Get user's reading history and ratings
                List<Rating> userRatings = ratingRepository.findByUserId(currentUser.getId());
                Set<UUID> ratedNovelIds = userRatings.stream()
                                .map(rating -> rating.getNovel().getId())
                                .collect(Collectors.toSet());

                // Get user's favorite genres and tags
                Set<String> favoriteGenres = preferences.getFavoriteGenres();
                Set<String> favoriteTags = preferences.getFavoriteTags();

                // Get novels matching user preferences
                List<Novel> candidateNovels = novelRepository.findByGenresInAndTagsIn(
                                new ArrayList<>(favoriteGenres),
                                new ArrayList<>(favoriteTags));

                // Filter out already rated novels
                candidateNovels = candidateNovels.stream()
                                .filter(novel -> !ratedNovelIds.contains(novel.getId()))
                                .collect(Collectors.toList());

                // Calculate recommendation scores
                Map<Novel, Double> novelScores = new HashMap<>();
                for (Novel novel : candidateNovels) {
                        double score = calculateRecommendationScore(novel, preferences, userRatings);
                        novelScores.put(novel, score);
                }

                // Sort novels by score
                List<Novel> sortedNovels = candidateNovels.stream()
                                .sorted((n1, n2) -> Double.compare(novelScores.get(n2), novelScores.get(n1)))
                                .collect(Collectors.toList());

                // Convert to page
                Page<Novel> page = new PageImpl<>(sortedNovels, pageable, sortedNovels.size());

                return new PageResponse<>(page);
        }

        private double calculateRecommendationScore(Novel novel, UserPreferences preferences,
                        List<Rating> userRatings) {
                double score = 0.0;

                // Genre match score (40% weight)
                double genreMatchScore = calculateGenreMatchScore(novel, preferences);
                score += genreMatchScore * 0.4;

                // Tag match score (20% weight)
                double tagMatchScore = calculateTagMatchScore(novel, preferences);
                score += tagMatchScore * 0.2;

                // Popularity score (20% weight)
                double popularityScore = calculatePopularityScore(novel);
                score += popularityScore * 0.2;

                // Similarity to user's rated novels (20% weight)
                double similarityScore = calculateSimilarityScore(novel, userRatings);
                score += similarityScore * 0.2;

                return score;
        }

        private double calculateGenreMatchScore(Novel novel, UserPreferences preferences) {
                Set<String> novelGenres = novel.getGenres().stream()
                                .map(Genre::getName)
                                .collect(Collectors.toSet());
                Set<String> userGenres = preferences.getFavoriteGenres();

                if (userGenres.isEmpty())
                        return 0.5; // Default score if no preferences

                long matchingGenres = novelGenres.stream()
                                .filter(userGenres::contains)
                                .count();

                return (double) matchingGenres / userGenres.size();
        }

        private double calculateTagMatchScore(Novel novel, UserPreferences preferences) {
                Set<String> novelTags = novel.getTags().stream()
                                .map(Tag::getName)
                                .collect(Collectors.toSet());
                Set<String> userTags = preferences.getFavoriteTags();

                if (userTags.isEmpty())
                        return 0.5; // Default score if no preferences

                long matchingTags = novelTags.stream()
                                .filter(userTags::contains)
                                .count();

                return (double) matchingTags / userTags.size();
        }

        private double calculatePopularityScore(Novel novel) {
                // Consider factors like view count, rating count, and average rating
                long viewCount = novel.getViews();
                long ratingCount = ratingRepository.countByNovelId(novel.getId());
                double avgRating = ratingRepository.getAverageRatingByNovelId(novel.getId());

                // Normalize scores
                double normalizedViewScore = Math.min(1.0, viewCount / 10000.0); // Cap at 10k views
                double normalizedRatingScore = Math.min(1.0, ratingCount / 100.0); // Cap at 100 ratings
                double normalizedAvgRatingScore = avgRating / 5.0; // Scale to 0-1

                // Weighted combination
                return (normalizedViewScore * 0.4) + (normalizedRatingScore * 0.3) + (normalizedAvgRatingScore * 0.3);
        }

        private double calculateSimilarityScore(Novel novel, List<Rating> userRatings) {
                if (userRatings.isEmpty())
                        return 0.5; // Default score if no ratings

                // Calculate similarity based on genres and tags of novels the user has rated
                // highly
                double similarityScore = 0.0;
                int count = 0;

                for (Rating rating : userRatings) {
                        if (rating.getScore() >= 4) { // Only consider highly rated novels
                                Novel ratedNovel = rating.getNovel();

                                // Genre similarity
                                Set<String> novelGenres = novel.getGenres().stream()
                                                .map(Genre::getName)
                                                .collect(Collectors.toSet());
                                Set<String> ratedGenres = ratedNovel.getGenres().stream()
                                                .map(Genre::getName)
                                                .collect(Collectors.toSet());
                                long matchingGenres = novelGenres.stream()
                                                .filter(ratedGenres::contains)
                                                .count();
                                double genreSimilarity = (double) matchingGenres /
                                                Math.max(novelGenres.size(), ratedGenres.size());

                                // Tag similarity
                                Set<String> novelTags = novel.getTags().stream()
                                                .map(Tag::getName)
                                                .collect(Collectors.toSet());
                                Set<String> ratedTags = novel.getTags().stream()
                                                .map(Tag::getName)
                                                .collect(Collectors.toSet());
                                long matchingTags = novelTags.stream()
                                                .filter(ratedTags::contains)
                                                .count();
                                double tagSimilarity = (double) matchingTags /
                                                Math.max(novelTags.size(), ratedTags.size());

                                // Combined similarity score
                                similarityScore += (genreSimilarity * 0.6) + (tagSimilarity * 0.4);
                                count++;
                        }
                }

                return count > 0 ? similarityScore / count : 0.5;
        }

        private UserPreferences createDefaultPreferences(User user) {
                UserPreferences preferences = new UserPreferences();
                preferences.setUser(user);
                preferences.setFavoriteGenres(new HashSet<>());
                preferences.setFavoriteTags(new HashSet<>());
                return userPreferencesRepository.save(preferences);
        }

        @Transactional(readOnly = true)
        public PageResponse<Novel> getPopularNovels(Pageable pageable) {
                return new PageResponse<>(novelRepository.findAllByOrderByViewsDesc(pageable));
        }

        @Transactional(readOnly = true)
        public PageResponse<Novel> getTopRatedNovels(Pageable pageable) {
                return new PageResponse<>(novelRepository.findAllByOrderByRatingDesc(pageable));
        }

        @Transactional(readOnly = true)
        public PageResponse<Novel> getNewReleases(Pageable pageable) {
                return new PageResponse<>(novelRepository.findAllByOrderByCreatedAtDesc(pageable));
        }

        @Transactional(readOnly = true)
        public PageResponse<Novel> getSimilarNovels(UUID novelId, Pageable pageable) {
                Novel novel = novelRepository.findById(novelId)
                                .orElseThrow(() -> new RuntimeException("Novel not found"));

                // Get novels with similar genres and tags
                List<Novel> similarNovels = novelRepository.findByGenresInAndTagsIn(
                                new ArrayList<>(novel.getGenres().stream()
                                                .map(Genre::getName)
                                                .collect(Collectors.toList())),
                                new ArrayList<>(novel.getTags().stream()
                                                .map(Tag::getName)
                                                .collect(Collectors.toList())));

                // Filter out the original novel
                similarNovels = similarNovels.stream()
                                .filter(n -> !n.getId().equals(novelId))
                                .collect(Collectors.toList());

                // Calculate similarity scores
                Map<Novel, Double> similarityScores = new HashMap<>();
                for (Novel similarNovel : similarNovels) {
                        double score = calculateNovelSimilarity(novel, similarNovel);
                        similarityScores.put(similarNovel, score);
                }

                // Sort by similarity score
                List<Novel> sortedNovels = similarNovels.stream()
                                .sorted((n1, n2) -> Double.compare(similarityScores.get(n2), similarityScores.get(n1)))
                                .collect(Collectors.toList());

                Page<Novel> page = new PageImpl<>(sortedNovels, pageable, sortedNovels.size());
                return new PageResponse<>(page);
        }

        private double calculateNovelSimilarity(Novel novel1, Novel novel2) {
                // Genre similarity (40% weight)
                Set<Genre> genres1 = novel1.getGenres();
                Set<Genre> genres2 = novel2.getGenres();
                long matchingGenres = genres1.stream()
                                .filter(genres2::contains)
                                .count();
                double genreSimilarity = (double) matchingGenres /
                                Math.max(genres1.size(), genres2.size());

                // Tag similarity (30% weight)
                Set<Tag> tags1 = novel1.getTags();
                Set<Tag> tags2 = novel2.getTags();
                long matchingTags = tags1.stream()
                                .filter(tags2::contains)
                                .count();
                double tagSimilarity = (double) matchingTags /
                                Math.max(tags1.size(), tags2.size());

                // Rating similarity (30% weight)
                double rating1 = ratingRepository.getAverageRatingByNovelId(novel1.getId());
                double rating2 = ratingRepository.getAverageRatingByNovelId(novel2.getId());
                double ratingSimilarity = 1.0 - Math.abs(rating1 - rating2) / 5.0;

                return (genreSimilarity * 0.4) + (tagSimilarity * 0.3) + (ratingSimilarity * 0.3);
        }
}