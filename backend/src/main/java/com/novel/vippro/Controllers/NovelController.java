package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.System.SystemJobDTO;
import com.novel.vippro.DTO.Novel.NovelCreateDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Novel.NovelSearchDTO;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.ChapterService;
import com.novel.vippro.Services.CommentService;
import com.novel.vippro.Services.EpubImportService;
import com.novel.vippro.Services.NovelService;
import com.novel.vippro.Services.SuggestService;
import com.novel.vippro.Services.ViewStatService;
import com.novel.vippro.DTO.Novel.SearchSuggestion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private SuggestService suggestService;

    @Autowired
    private EpubImportService epubImportService;

    @Autowired
    private ViewStatService viewStatService;

    private static final Logger logger = LoggerFactory.getLogger(NovelController.class);

    @Operation(summary = "Get all novels", description = "Retrieves a paginated list of all novels with sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size parameters")
    })
    @GetMapping
    public ControllerResponse<PageResponse<NovelDTO>> getAllNovels(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.getAllNovels(pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Get novel by ID", description = "Retrieve detailed information about a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novel found"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{id}")
    public ControllerResponse<NovelDTO> getNovelById(
            @Parameter(description = "Novel ID") @PathVariable UUID id) {
        NovelDTO novel = novelService.getNovelById(id);
        return ControllerResponse.success("Novel retrieved successfully", novel);
    }

    @GetMapping("/slug/{slug}")
    public ControllerResponse<NovelDTO> getNovelBySlug(
            @Parameter(description = "Novel slug") @PathVariable String slug) {
        NovelDTO novel = novelService.getNovelBySlug(slug);
        return ControllerResponse.success("Novel retrieved successfully", novel);
    }

    @Operation(summary = "Get novels by genre", description = "Retrieves novels filtered by genre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/genre/{genre}")
    public ControllerResponse<PageResponse<NovelDTO>> getNovelsByGenre(
            @Parameter(description = "Category name or slug") @PathVariable UUID genre,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<NovelDTO> novels = novelService.getNovelsByGenre(genre, pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Get novels by tag", description = "Retrieves novels filtered by tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/tag/{tag}")
    public ControllerResponse<PageResponse<NovelDTO>> getNovelsByTag(
            @Parameter(description = "Category name or slug") @PathVariable UUID tag,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<NovelDTO> novels = novelService.getNovelsByTag(tag, pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Get novels by category", description = "Retrieves novels filtered by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{category}")
    public ControllerResponse<PageResponse<NovelDTO>> getNovelsByCategory(
            @Parameter(description = "Category name or slug") @PathVariable UUID category,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<NovelDTO> novels = novelService.getNovelsByCategory(category, pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Get novels by status", description = "Retrieves novels filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved novels"),
            @ApiResponse(responseCode = "404", description = "Status not found")
    })
    @GetMapping("/status/{status}")
    public ControllerResponse<PageResponse<NovelDTO>> getNovelsByStatus(
            @Parameter(description = "Status") @PathVariable String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.getNovelsByStatus(status, pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Search novels", description = "Search novels by combining keyword, title, author, category, or genre filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ControllerResponse<PageResponse<NovelDTO>> searchNovels(
            @Parameter(description = "Search filters") @ModelAttribute NovelSearchDTO searchDTO,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.searchNovels(searchDTO, pageable);
        return ControllerResponse.success("Novels retrieved successfully", novels);
    }

    @Operation(summary = "Get hot novels", description = "Get popular novels based on view count")
    @GetMapping("/hot")
    public ControllerResponse<PageResponse<NovelDTO>> getHotNovels(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.getHotNovels(pageable);
        return ControllerResponse.success("Hot novels retrieved successfully", novels);
    }

    @Operation(summary = "Get top rated novels", description = "Get novels sorted by rating")
    @GetMapping("/top-rated")
    public ControllerResponse<PageResponse<NovelDTO>> getTopRatedNovels(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.getTopRatedNovels(pageable);
        return ControllerResponse.success("Top rated novels retrieved successfully", novels);
    }

    // latest updated novels
    @Operation(summary = "Get latest updates novels", description = "Get novels sorted by latest update")
    @GetMapping("/latest-updates")
    public ControllerResponse<PageResponse<NovelDTO>> getLatestUpdatedNovels(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NovelDTO> novels = novelService.getLatestUpdatedNovels(pageable);
        return ControllerResponse.success("Latest updated novels retrieved successfully", novels);
    }

    @Operation(summary = "Get chapters by novel", description = "Retrieve chapters of a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chapters"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{novelId}/chapters")
    public ControllerResponse<PageResponse<Chapter>> getChaptersByNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID novelId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<Chapter> chapters = chapterService.getChaptersByNovel(novelId, pageable);
        return ControllerResponse.success("Chapters retrieved successfully", chapters);
    }

    @Operation(summary = "Get comments by novel", description = "Retrieve comments of a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/{novelId}/comments")
    public ControllerResponse<PageResponse<CommentDTO>> getCommentsByNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID novelId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<CommentDTO> comments = commentService.getNovelComments(novelId, pageable);
        return ControllerResponse.success("Comments retrieved successfully", comments);
    }

    @Operation(summary = "Create novel", description = "Create a new novel", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novel created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<NovelDTO> createNovel(@RequestBody NovelCreateDTO novelDTO) {
        NovelDTO createdNovel = novelService.createNovel(novelDTO);
        return ControllerResponse.success("Novel created successfully", createdNovel);
    }

    @Operation(summary = "Import EPUB and create novel", description = "Upload an EPUB file and import it as a novel (creates novel + chapters)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/import-epub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<SystemJobDTO> importEpub(
            @RequestPart("epub") MultipartFile epub,
            @RequestParam(value = "slug") String slug,
            @RequestParam(value = "status", required = false) String status) {
        try {
            SystemJobDTO job = epubImportService.queueNewNovelImport(epub, slug, status);
            return ControllerResponse.success("EPUB import queued successfully", job);
        } catch (Exception e) {
            logger.error("Failed to queue epub import: {}", e.getMessage(), e);
            return ControllerResponse.error("Failed to queue epub import: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "Add chapters from EPUB", description = "Add chapters to a novel from an EPUB file", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/{novelId}/import-epub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<SystemJobDTO> addChaptersFromEpub(
            @Parameter(description = "Novel ID") @PathVariable UUID novelId,
            @RequestPart("epub") MultipartFile epub) {
        try {
            SystemJobDTO job = epubImportService.queueChaptersImport(novelId, epub);
            return ControllerResponse.success("EPUB import queued successfully", job);
        } catch (Exception e) {
            logger.error("Failed to queue chapter import from epub: {}", e.getMessage(), e);
            return ControllerResponse.error("Failed to queue chapter import: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "Update novel", description = "Update an existing novel", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<NovelDTO> updateNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID id,
            @RequestBody NovelCreateDTO novelDTO) {
        NovelDTO updatedNovel = novelService.updateNovel(id, novelDTO);
        return ControllerResponse.success("Novel updated successfully", updatedNovel);
    }

    @Operation(summary = "Update novel cover image", description = "Update the cover image of an existing novel", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<NovelDTO> updateNovelCover(
            @Parameter(description = "Novel ID") @PathVariable UUID id,
            @RequestPart("coverImage") MultipartFile coverImage) {
        NovelDTO updatedNovel = novelService.updateNovelCover(id, coverImage);
        return ControllerResponse.success("Novel cover image updated successfully", updatedNovel);
    }

    @Operation(summary = "Delete novel", description = "Delete an existing novel", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ControllerResponse<Void> deleteNovel(
            @Parameter(description = "Novel ID") @PathVariable UUID id) {
        novelService.deleteNovel(id);
        return ControllerResponse.success("Novel deleted successfully", null);
    }

    @Operation(summary = "Update novel rating", description = "Update the rating of a novel")
    @PutMapping("/{id}/rating")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<NovelDTO> updateRating(
            @Parameter(description = "Novel ID") @PathVariable UUID id,
            @Parameter(description = "New rating value (1-5)") @RequestParam int rating) {
        NovelDTO updatedNovel = novelService.updateRating(id, rating);
        return ControllerResponse.success("Novel rating updated successfully", updatedNovel);
    }

    @Operation(summary = "Reindex all novels", description = "Reindex all novels in the search repository", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ControllerResponse<Void> reindexAllNovels() {
        novelService.reindexAllNovels();
        return ControllerResponse.success("All novels reindexed successfully", null);
    }

    @Operation(summary = "Suggest titles", description = "Real-time suggestions for search box")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/suggest")
    public List<SearchSuggestion> suggest(
            @Parameter(description = "partial keyword") @RequestParam("q") String q,
            @Parameter(description = "max results") @RequestParam(value = "limit", defaultValue = "8") int limit) {
        if (q == null || q.isBlank())
            return List.of();
        limit = Math.min(Math.max(1, limit), 20);
        return suggestService.suggest(q.trim(), limit);
    }

    @GetMapping("/{id}/views")
    public ControllerResponse<Map<String, Long>> getNovelViewStats(
            @PathVariable UUID id) {
        Map<String, Long> stats = viewStatService.getNovelViewStats(id);
        return ControllerResponse.success("View statistics retrieved successfully", stats);
    }
}
