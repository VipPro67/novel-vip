package com.novel.vippro.Controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingStatsDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.ReadingHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reading-history")
@Tag(name = "Reading History", description = "User reading history management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class ReadingHistoryController {

	@Autowired
	private ReadingHistoryService readingHistoryService;

	@Operation(summary = "Get user reading history", description = "Get paginated list of user's reading history")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reading history retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Not authenticated")
	})
	@GetMapping
	public ControllerResponse<PageResponse<ReadingHistoryDTO>> getReadingHistory(
			@Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
		PageResponse<ReadingHistoryDTO> history = readingHistoryService.getUserNovelReadingHistory(pageable);
		return ControllerResponse.success("Reading history retrieved successfully", history);
	}

	@Operation(summary = "Get novel reading history", description = "Get paginated list of reading history for a specific novel")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reading history retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "404", description = "Novel not found")
	})
	@GetMapping("/novel/{novelId}")
	public ControllerResponse<PageResponse<ReadingHistoryDTO>> getNovelReadingHistory(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
			@Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
		PageResponse<ReadingHistoryDTO> history = readingHistoryService.getNovelReadingHistory(novelId,
				pageable);
		return ControllerResponse.success("Novel reading history retrieved successfully", history);
	}


	@PostMapping("/novel/{novelId}")
	public ControllerResponse<ReadingHistoryDTO> updateReadingProgress(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
			@Parameter(description = "Last read chapter index", required = true) @RequestParam Integer lastReadChapterIndex
			) {
		ReadingHistoryDTO dto = readingHistoryService.updateReadingProgress(novelId, lastReadChapterIndex);
		return ControllerResponse.success("Reading progress updated", dto);
	}

	@Operation(summary = "Get last read chapter", description = "Get the last read chapter for a novel")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Last read chapter retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "404", description = "Novel not found or no reading history")
	})
	@GetMapping("/novel/{novelId}/last-read")
	public ControllerResponse<ReadingHistoryDTO> getLastReadChapter(
			@Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
		ReadingHistoryDTO lastRead = readingHistoryService.getLastReadChapter(novelId);
		return ControllerResponse.success("Last read chapter retrieved successfully", lastRead);
	}

	@Operation(summary = "Delete reading history", description = "Delete a specific reading history entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reading history deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Not authenticated"),
			@ApiResponse(responseCode = "403", description = "Not authorized"),
			@ApiResponse(responseCode = "404", description = "Reading history not found")
	})
	@DeleteMapping("/{id}")
	public ControllerResponse<Void> deleteReadingHistory(
			@Parameter(description = "Reading history ID", required = true) @PathVariable UUID id) {
		readingHistoryService.deleteReadingHistory(id);
		return ControllerResponse.success("Reading history deleted successfully", null);
	}

	@Operation(summary = "Clear reading history", description = "Delete all reading history for the current user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reading history cleared successfully"),
			@ApiResponse(responseCode = "401", description = "Not authenticated")
	})
	@DeleteMapping("/clear")
	public ControllerResponse<Void> clearReadingHistory() {
		readingHistoryService.clearReadingHistory();
		return ControllerResponse.success("Reading history cleared successfully", null);
	}

	@Operation(summary = "Get reading statistics", description = "Get reading statistics for the current user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reading statistics retrieved"),
			@ApiResponse(responseCode = "401", description = "Not authenticated")
	})
	@GetMapping("/stats")
	public ControllerResponse<ReadingStatsDTO> getReadingStats() {
		ReadingStatsDTO stats = readingHistoryService.getReadingStats();
		return ControllerResponse.success("Reading statistics retrieved", stats);
	}
}