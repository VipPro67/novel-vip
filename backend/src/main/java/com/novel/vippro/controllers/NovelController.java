package com.novel.vippro.controllers;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.dto.CommentDTO;
import com.novel.vippro.dto.NovelCreateDTO;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.NovelService;

import jakarta.validation.Valid;

import com.novel.vippro.services.ChapterService;
import com.novel.vippro.services.CommentService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/novels")
@Tag(name = "Novel", description = "Novel management APIs")
@SecurityRequirement(name = "bearerAuth")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CommentService commentService;

    @Operation(summary = "Get all novels", description = "Retrieves a paginated list of all novels with sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size parameters")
    })
    @GetMapping
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getAllNovels(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (views, rating, createdAt)") @RequestParam(defaultValue = "views") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<NovelDTO> novels = novelService.getAllNovels(pageable);
        return ResponseEntity.ok(ControllerResponse.success("Novels retrieved successfully", novels));
    }

    @Operation(summary = "Get novel by ID", description = "Retrieve detailed information about a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novel found"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ControllerResponse<NovelDTO>> getNovelById(
            @Parameter(description = "Novel ID") @PathVariable UUID id) {
        NovelDTO novel = novelService.getNovelById(id);
        return ResponseEntity.ok(ControllerResponse.success("Novel retrieved successfully", novel));
    }

    @Operation(summary = "Get novels by category", description = "Retrieves novels filtered by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getNovelsByCategory(
            @Parameter(description = "Category name or slug") @PathVariable String category,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<NovelDTO> novels = novelService.getNovelsByCategory(category, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Novels retrieved successfully", novels));
    }

    @Operation(summary = "Get novels by status", description = "Retrieves novels filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Status not found")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getNovelsByStatus(
            @Parameter(description = "Status") @PathVariable String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getNovelsByStatus(status, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Novels retrieved successfully", novels));
    }

    @Operation(summary = "Search novels", description = "Search novels by keyword in title and description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> searchNovels(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.searchNovels(keyword, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Novels retrieved successfully", novels));
    }

    @Operation(summary = "Get hot novels", description = "Get popular novels based on view count")
    @GetMapping("/hot")
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getHotNovels(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getHotNovels(pageable);
        return ResponseEntity.ok(ControllerResponse.success("Hot novels retrieved successfully", novels));
    }

    @Operation(summary = "Get top rated novels", description = "Get novels sorted by rating")
    @GetMapping("/top-rated")
    public ResponseEntity<ControllerResponse<Page<NovelDTO>>> getTopRatedNovels(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getTopRatedNovels(pageable);
        return ResponseEntity.ok(ControllerResponse.success("Top rated novels retrieved successfully", novels));
    }

    @Operation(summary = "Get chapters by novel", description = "Retrieve chapters of a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chapters"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{novelId}/chapters")
    public ResponseEntity<ControllerResponse<Page<Chapter>>> getChaptersByNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID novelId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Chapter> chapters = chapterService.getChaptersByNovel(novelId, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Chapters retrieved successfully", chapters));
    }

    @Operation(summary = "Get comments by novel", description = "Retrieve comments of a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{novelId}/comments")
    public ResponseEntity<ControllerResponse<Page<CommentDTO>>> getCommentsByNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID novelId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> comments = commentService.getNovelComments(novelId, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Comments retrieved successfully", comments));
    }

    @Operation(summary = "Create novel", description = "Create a new novel", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novel created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<ControllerResponse<NovelDTO>> createNovel(
            @Parameter(description = "Novel details", required = true) @Valid @RequestBody NovelCreateDTO novelDTO) {
        NovelDTO createdNovel = novelService.createNovel(novelDTO);
        return ResponseEntity.ok(ControllerResponse.success("Novel created successfully", createdNovel));
    }

    @Operation(summary = "Update novel", description = "Update an existing novel", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<ControllerResponse<NovelDTO>> updateNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID id,
            @Parameter(description = "Updated novel details") @Valid @RequestBody NovelCreateDTO novelDTO) {
        NovelDTO updatedNovel = novelService.updateNovel(id, novelDTO);
        return ResponseEntity.ok(ControllerResponse.success("Novel updated successfully", updatedNovel));
    }

    @Operation(summary = "Delete novel", description = "Delete an existing novel", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<ControllerResponse<Void>> deleteNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID id) {
        novelService.deleteNovel(id);
        return ResponseEntity.ok(ControllerResponse.success("Novel deleted successfully", null));
    }

    @Operation(summary = "Increment novel views", description = "Increment the view count of a novel")
    @PutMapping("/{id}/increment-views")
    public ResponseEntity<ControllerResponse<NovelDTO>> incrementViews(
            @Parameter(description = "Novel ID") @PathVariable UUID id) {
        NovelDTO updatedNovel = novelService.incrementViews(id);
        return ResponseEntity.ok(ControllerResponse.success("Novel views incremented successfully", updatedNovel));
    }

    @Operation(summary = "Update novel rating", description = "Update the rating of a novel")
    @PutMapping("/{id}/rating")
    public ResponseEntity<ControllerResponse<NovelDTO>> updateRating(
            @Parameter(description = "Novel ID") @PathVariable UUID id,
            @Parameter(description = "New rating value (1-5)") @RequestParam int rating) {
        NovelDTO updatedNovel = novelService.updateRating(id, rating);
        return ResponseEntity.ok(ControllerResponse.success("Novel rating updated successfully", updatedNovel));
    }
}