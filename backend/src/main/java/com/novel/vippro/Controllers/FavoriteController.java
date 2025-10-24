package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.FavoriteService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public ControllerResponse<PageResponse<NovelDTO>> getUserFavorites(
			@Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
		PageResponse<NovelDTO> favorites = favoriteService.getUserFavorites(pageable);
		return ControllerResponse.success("Favorites retrieved successfully", favorites);
	}

	@Operation(summary = "Add to favorites", description = "Add a novel to user's favorites")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Novel added to favorites"),
			@ApiResponse(responseCode = "400", description = "Novel already in favorites"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "404", description = "Novel not found")
	})
	@PostMapping("/{novelId}")
	public ControllerResponse<Void> addToFavorites(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
		favoriteService.addToFavorites(novelId);
		return ControllerResponse.success("Novel added to favorites", null);
	}

	@Operation(summary = "Remove from favorites", description = "Remove a novel from user's favorites")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Novel removed from favorites"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "404", description = "Favorite not found")
	})
	@DeleteMapping("/{novelId}")
	public ControllerResponse<Void> removeFromFavorites(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
		favoriteService.removeFromFavorites(novelId);
		return ControllerResponse.success("Novel removed from favorites", null);
	}

	@Operation(summary = "Check favorite status", description = "Check if a novel is in user's favorites")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Favorite status retrieved"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "404", description = "Novel not found")
	})
	@GetMapping("/{novelId}/status")
	public ControllerResponse<Boolean> checkFavoriteStatus(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
		boolean isFavorite = favoriteService.isFavorite(novelId);
		return ControllerResponse.success("Favorite status retrieved", isFavorite);
	}

	@Operation(summary = "Get favorite count", description = "Get the number of users who favorited a novel")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Favorite count retrieved"),
			@ApiResponse(responseCode = "404", description = "Novel not found")
	})
	@GetMapping("/{novelId}/count")
	public ControllerResponse<Long> getFavoriteCount(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
		long count = favoriteService.getFavoriteCount(novelId);
		return ControllerResponse.success("Favorite count retrieved", count);
	}
}