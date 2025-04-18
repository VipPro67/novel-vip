package com.novel.vippro.controllers;

import com.novel.vippro.dto.BookmarkDTO;
import com.novel.vippro.dto.BookmarkCreateDTO;
import com.novel.vippro.dto.BookmarkUpdateDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.services.BookmarkService;
import com.novel.vippro.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/bookmarks")
@Tag(name = "Bookmarks", description = "Bookmark management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class BookmarkController {

        @Autowired
        private BookmarkService bookmarkService;

        @Autowired
        private UserService userService;

        @Operation(summary = "Get user bookmarks", description = "Get all bookmarks for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping
        public ControllerResponse<PageResponse<BookmarkDTO>> getUserBookmarks(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                UUID userId = userService.getCurrentUserId();
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<BookmarkDTO> bookmarks = bookmarkService.getUserBookmarks(userId, pageable);
                return ControllerResponse.success("Bookmarks retrieved successfully", bookmarks);
        }

        @Operation(summary = "Get novel bookmarks", description = "Get all bookmarks for a specific novel")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}")
        public ControllerResponse<List<BookmarkDTO>> getNovelBookmarks(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId) {
                List<BookmarkDTO> bookmarks = bookmarkService.getNovelBookmarks(novelId);
                return ControllerResponse.success("Bookmarks retrieved successfully", bookmarks);
        }

        @Operation(summary = "Create bookmark", description = "Create a new bookmark")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid bookmark data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Novel or chapter not found")
        })
        @PostMapping
        public ControllerResponse<BookmarkDTO> createBookmark(
                        @Parameter(description = "Bookmark details", required = true) @Valid @RequestBody BookmarkCreateDTO bookmarkDTO) {
                UUID userId = userService.getCurrentUserId();
                BookmarkDTO createdBookmark = bookmarkService.createBookmark(userId, bookmarkDTO);
                return ControllerResponse.success("Bookmark created successfully", createdBookmark);
        }

        @Operation(summary = "Update bookmark", description = "Update an existing bookmark")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid bookmark data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Bookmark not found")
        })
        @PutMapping("/{id}")
        public ControllerResponse<BookmarkDTO> updateBookmark(
                        @Parameter(description = "Bookmark ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Updated bookmark details", required = true) @Valid @RequestBody BookmarkUpdateDTO bookmarkDTO) {
                BookmarkDTO updatedBookmark = bookmarkService.updateBookmark(id, bookmarkDTO);
                return ControllerResponse.success("Bookmark updated successfully", updatedBookmark);
        }

        @Operation(summary = "Delete bookmark", description = "Delete an existing bookmark")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookmark deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Bookmark not found")
        })
        @DeleteMapping("/{id}")
        public ControllerResponse<Void> deleteBookmark(
                        @Parameter(description = "Bookmark ID", required = true) @PathVariable UUID id) {
                bookmarkService.deleteBookmark(id);
                return ControllerResponse.success("Bookmark deleted successfully", null);
        }
}
