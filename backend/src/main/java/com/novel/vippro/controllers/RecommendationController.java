package com.novel.vippro.controllers;

import com.novel.vippro.models.Novel;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.RecommendationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Novel recommendation APIs")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

        @Autowired
        private RecommendationService recommendationService;

        @Operation(summary = "Get personalized recommendations", description = "Get novel recommendations based on user's reading history and preferences")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/personalized")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ControllerResponse<Page<Novel>>> getPersonalizedRecommendations(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Novel> recommendations = recommendationService.getPersonalizedRecommendations(pageable);
                return ResponseEntity.ok(
                                ControllerResponse.success("Recommendations retrieved successfully", recommendations));
        }

        @Operation(summary = "Get similar novels", description = "Get novels similar to a specific novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Similar novels retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/similar/{novelId}")
        public ResponseEntity<ControllerResponse<Page<Novel>>> getSimilarNovels(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Novel> similarNovels = recommendationService.getSimilarNovels(novelId, pageable);
                return ResponseEntity
                                .ok(ControllerResponse.success("Similar novels retrieved successfully", similarNovels));
        }

        @Operation(summary = "Get popular novels", description = "Get most popular novels based on rating")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Popular novels retrieved successfully")
        })
        @GetMapping("/popular")
        public ResponseEntity<ControllerResponse<Page<Novel>>> getPopularNovels(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Novel> popularNovels = recommendationService.getPopularNovels(pageable);
                return ResponseEntity
                                .ok(ControllerResponse.success("Popular novels retrieved successfully", popularNovels));
        }
}