package com.novel.vippro.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

import com.novel.vippro.dto.ReviewCreateDTO;
import com.novel.vippro.dto.ReviewDTO;
import com.novel.vippro.dto.ReviewSummaryDTO;
import com.novel.vippro.dto.ReviewUpdateDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// ...existing imports...

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Novel review management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

        @Autowired
        private ReviewService reviewService;

        @Operation(summary = "Get reviews by novel", description = "Retrieve all reviews for a specific novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}")
        public ResponseEntity<ControllerResponse<Page<ReviewDTO>>> getReviewsByNovel(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReviewDTO> reviews = reviewService.getReviewsByNovel(novelId, pageable);
                return ResponseEntity.ok(ControllerResponse.success("Reviews retrieved successfully", reviews));
        }

        @Operation(summary = "Get reviews by user", description = "Retrieve all reviews made by a specific user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/user/{userId}")
        public ResponseEntity<ControllerResponse<Page<ReviewDTO>>> getReviewsByUser(
                        @Parameter(description = "User ID", required = true) @PathVariable UUID userId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReviewDTO> reviews = reviewService.getReviewsByUser(userId, pageable);
                return ResponseEntity.ok(ControllerResponse.success("Reviews retrieved successfully", reviews));
        }

        @Operation(summary = "Create review", description = "Create a new review for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Review created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid review data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ControllerResponse<ReviewDTO>> createReview(
                        @Parameter(description = "Review details", required = true) @Valid @RequestBody ReviewCreateDTO reviewDTO) {
                ReviewDTO createdReview = reviewService.createReview(reviewDTO);
                return ResponseEntity.ok(ControllerResponse.success("Review created successfully", createdReview));
        }

        @Operation(summary = "Update review", description = "Update an existing review")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid review data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Review not found")
        })
        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ControllerResponse<ReviewDTO>> updateReview(
                        @Parameter(description = "Review ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Updated review details", required = true) @Valid @RequestBody ReviewUpdateDTO reviewDTO) {
                ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);
                return ResponseEntity.ok(ControllerResponse.success("Review updated successfully", updatedReview));
        }

        @Operation(summary = "Delete review", description = "Delete an existing review")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Review not found")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ControllerResponse<Void>> deleteReview(
                        @Parameter(description = "Review ID", required = true) @PathVariable UUID id) {
                reviewService.deleteReview(id);
                return ResponseEntity.ok(ControllerResponse.success("Review deleted successfully", null));
        }

        @Operation(summary = "Get review ratings summary", description = "Get summary statistics of reviews for a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rating summary retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}/summary")
        public ResponseEntity<ControllerResponse<ReviewSummaryDTO>> getReviewSummary(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                ReviewSummaryDTO summary = reviewService.getReviewSummary(novelId);
                return ResponseEntity.ok(ControllerResponse.success("Review summary retrieved successfully", summary));
        }
}