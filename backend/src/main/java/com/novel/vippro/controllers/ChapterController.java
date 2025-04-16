package com.novel.vippro.controllers;

import com.novel.vippro.dto.ChapterCreateDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.dto.ChapterListDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chapters")
@Tag(name = "Chapters", description = "Chapter management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Operation(summary = "Get chapters by novel", description = "Get all chapters for a specific novel with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapters retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<ControllerResponse<Page<ChapterListDTO>>> getChaptersByNovel(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChapterListDTO> chapters = chapterService.getChaptersByNovelDTO(novelId, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Chapters retrieved successfully", chapters));
    }

    @Operation(summary = "Get chapter by number", description = "Get a specific chapter by its number within a novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter or novel not found"),
            @ApiResponse(responseCode = "400", description = "Error retrieving chapter content")
    })
    @GetMapping("/novel/{novelId}/chapter/{chapterNumber}")
    public ResponseEntity<ControllerResponse<ChapterDetailDTO>> getChapterByNumber(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Chapter number", required = true) @PathVariable Integer chapterNumber) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterByNumberDTO(novelId, chapterNumber);
            return ResponseEntity.ok(ControllerResponse.success("Chapter retrieved successfully", chapter));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ControllerResponse.error("Failed to get chapter content: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Get chapter by ID", description = "Get detailed information about a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Error retrieving chapter content")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ControllerResponse<ChapterDetailDTO>> getChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterDetailDTO(id);
            return ResponseEntity.ok(ControllerResponse.success("Chapter retrieved successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ControllerResponse.error("Failed to get chapter content: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Get chapter audio", description = "Get or generate audio content for a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audio content retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Error generating audio content")
    })
    @GetMapping("/{id}/audio")
    public ResponseEntity<ControllerResponse<ChapterDetailDTO>> getChapterAudio(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterAudio(id);
            return ResponseEntity.ok(ControllerResponse.success("Chapter audio retrieved successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ControllerResponse.error("Failed to retrieve chapter audio: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Create new chapter", description = "Create a new chapter for a novel", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or error saving content"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<ControllerResponse<ChapterListDTO>> createChapter(
            @Parameter(description = "Chapter details", required = true) @RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.createChapterDTO(chapterDTO);
            return ResponseEntity.ok(ControllerResponse.success("Chapter created successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ControllerResponse.error("Failed to create chapter: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Update chapter", description = "Update an existing chapter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter updated successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input or error saving content"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ControllerResponse<ChapterListDTO>> updateChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated chapter details", required = true) @RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.updateChapterDTO(id, chapterDTO);
            return ResponseEntity.ok(ControllerResponse.success("Chapter updated successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ControllerResponse.error("Failed to update chapter: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Delete chapter", description = "Delete an existing chapter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ControllerResponse<Void>> deleteChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.ok(ControllerResponse.success("Chapter deleted successfully", null));
    }
}