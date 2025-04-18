package com.novel.vippro.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.payload.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.novel.vippro.dto.RatingDTO;
import com.novel.vippro.dto.RatingCreateDTO;
import com.novel.vippro.dto.RatingStatsDTO;
import com.novel.vippro.dto.RatingSummaryDTO;
import com.novel.vippro.dto.RatingUpdateDTO;
import com.novel.vippro.services.RatingService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ratings")
@Tag(name = "Ratings", description = "Novel rating management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

        private final RatingService ratingService;

        public RatingController(RatingService ratingService) {
                this.ratingService = ratingService;
        }

        @Operation(summary = "Get novel ratings", description = "Get all ratings for a specific novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}")
        public ControllerResponse<PageResponse<RatingDTO>> getNovelRatings(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<RatingDTO> ratings = ratingService.getNovelRatings(novelId, pageable);
                return ControllerResponse.success("Ratings retrieved successfully", ratings);
        }

        @Operation(summary = "Rate novel", description = "Add or update rating for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating added/updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid rating data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @PostMapping("/novel/{novelId}")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<RatingDTO> rateNovel(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "Rating details", required = true) @Valid @RequestBody RatingCreateDTO ratingDTO) {
                RatingDTO rating = ratingService.rateNovel(novelId, ratingDTO);
                return ControllerResponse.success("Rating submitted successfully", rating);
        }

        @Operation(summary = "Get user rating", description = "Get user's rating for a specific novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Rating not found")
        })
        @GetMapping("/novel/{novelId}/user")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<RatingDTO> getUserRating(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                RatingDTO rating = ratingService.getUserRating(novelId);
                return ControllerResponse.success("Rating retrieved successfully", rating);
        }

        @Operation(summary = "Delete rating", description = "Delete user's rating for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Rating not found")
        })
        @DeleteMapping("/novel/{novelId}")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<Void> deleteRating(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                ratingService.deleteRating(novelId);
                return ControllerResponse.success("Rating deleted successfully", null);
        }

        @Operation(summary = "Get rating statistics", description = "Get rating statistics for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}/stats")
        public ControllerResponse<RatingStatsDTO> getRatingStats(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                RatingStatsDTO stats = ratingService.getRatingStats(novelId);
                return ControllerResponse.success("Rating statistics retrieved successfully", stats);
        }

        @Operation(summary = "Get rating summary", description = "Get rating summary statistics for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating summary retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}/summary")
        public ControllerResponse<RatingSummaryDTO> getRatingSummary(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                RatingSummaryDTO summary = ratingService.getRatingSummary(novelId);
                return ControllerResponse.success("Rating summary retrieved successfully", summary);
        }

        @Operation(summary = "Update rating", description = "Update an existing rating")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid rating value"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Rating not found")
        })
        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<RatingDTO> updateRating(
                        @Parameter(description = "Rating ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Updated rating details", required = true) @Valid @RequestBody RatingUpdateDTO ratingDTO) {
                RatingDTO rating = ratingService.updateRating(id, ratingDTO);
                return ControllerResponse.success("Rating updated successfully", rating);
        }

        @Operation(summary = "Get user ratings", description = "Get all ratings submitted by a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User ratings retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/user/{userId}")
        public ControllerResponse<PageResponse<RatingDTO>> getUserRatings(
                        @Parameter(description = "User ID", required = true) @PathVariable UUID userId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<RatingDTO> ratings = ratingService.getUserRatings(userId, pageable);
                return ControllerResponse.success("User ratings retrieved successfully", ratings);
        }

        @Operation(summary = "Get user rating for novel", description = "Get a specific user's rating for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Rating not found")
        })
        @GetMapping("/novel/{novelId}/user/{userId}")
        public ControllerResponse<RatingDTO> getUserNovelRating(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "User ID", required = true) @PathVariable UUID userId) {
                RatingDTO rating = ratingService.getUserNovelRating(novelId, userId);
                return ControllerResponse.success("Rating retrieved successfully", rating);
        }

        @Operation(summary = "Get rating distribution", description = "Get the distribution of ratings for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating distribution retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}/distribution")
        public ControllerResponse<Map<Integer, Long>> getRatingDistribution(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                Map<Integer, Long> distribution = ratingService.getRatingDistribution(novelId);
                return ControllerResponse.success("Rating distribution retrieved successfully",
                                distribution);
        }
}