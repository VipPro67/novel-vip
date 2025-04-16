package com.novel.vippro.controllers;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.FavoriteService;

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
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Novel favorites management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

        @Autowired
        private FavoriteService favoriteService;

        @Operation(summary = "Get user favorites", description = "Get all favorited novels for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping
        public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getUserFavorites(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<NovelDTO> favorites = favoriteService.getUserFavorites(pageable);
                return ResponseEntity.ok(ControllerResponse.success("Favorites retrieved successfully", favorites));
        }

        @Operation(summary = "Add to favorites", description = "Add a novel to user's favorites")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Novel added to favorites"),
                        @ApiResponse(responseCode = "400", description = "Novel already in favorites"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @PostMapping("/{novelId}")
        public ResponseEntity<ControllerResponse<Void>> addToFavorites(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                favoriteService.addToFavorites(novelId);
                return ResponseEntity.ok(ControllerResponse.success("Novel added to favorites", null));
        }

        @Operation(summary = "Remove from favorites", description = "Remove a novel from user's favorites")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Novel removed from favorites"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Favorite not found")
        })
        @DeleteMapping("/{novelId}")
        public ResponseEntity<ControllerResponse<Void>> removeFromFavorites(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                favoriteService.removeFromFavorites(novelId);
                return ResponseEntity.ok(ControllerResponse.success("Novel removed from favorites", null));
        }

        @Operation(summary = "Check favorite status", description = "Check if a novel is in user's favorites")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Favorite status retrieved"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/{novelId}/status")
        public ResponseEntity<ControllerResponse<Boolean>> checkFavoriteStatus(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                boolean isFavorite = favoriteService.isFavorite(novelId);
                return ResponseEntity.ok(ControllerResponse.success("Favorite status retrieved", isFavorite));
        }

        @Operation(summary = "Get favorite count", description = "Get the number of users who favorited a novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Favorite count retrieved"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/{novelId}/count")
        public ResponseEntity<ControllerResponse<Long>> getFavoriteCount(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                long count = favoriteService.getFavoriteCount(novelId);
                return ResponseEntity.ok(ControllerResponse.success("Favorite count retrieved", count));
        }
}