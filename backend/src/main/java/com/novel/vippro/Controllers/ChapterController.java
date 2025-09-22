package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.Chapter.UploadChapterResult;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.AdvancedChapterUploadService;
import com.novel.vippro.Services.ChapterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chapters")
@Tag(name = "Chapters", description = "Chapter management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private AdvancedChapterUploadService advancedChapterUploadService;

    @Operation(summary = "Get chapters by novel", description = "Get all chapters for a specific novel with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapters retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/novel/{novelId}")
    public ControllerResponse<PageResponse<ChapterDTO>> getChaptersByNovel(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<ChapterDTO> chapters = chapterService.getChaptersByNovelDTO(novelId, pageable);
        return ControllerResponse.success("Chapters retrieved successfully", chapters);
    }

    @Operation(summary = "Get chapter by number", description = "Get a specific chapter by its number within a novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter or novel not found"),
            @ApiResponse(responseCode = "400", description = "Error retrieving chapter content")
    })
    @GetMapping("/novel/{novelId}/chapter/{chapterNumber}")
    public ControllerResponse<ChapterDetailDTO> getChapterByNumber(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Chapter number", required = true) @PathVariable Integer chapterNumber) {
        ChapterDetailDTO chapter = chapterService.getChapterByNumberDTO(novelId, chapterNumber);
        return ControllerResponse.success("Chapter retrieved successfully", chapter);
    }

    @Operation(summary = "Get chapter by number 2", description = "Get a specific chapter by its number within a novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter or novel not found"),
            @ApiResponse(responseCode = "400", description = "Error retrieving chapter content")
    })
    @GetMapping("/novel/slug/{slug}/chapter/{chapterNumber}")
    public ControllerResponse<ChapterDetailDTO> getChapterByNumber2(
            @Parameter(description = "Novel slug", required = true) @PathVariable String slug,
            @Parameter(description = "Chapter number", required = true) @PathVariable Integer chapterNumber) {
        ChapterDetailDTO chapter = chapterService.getChapterByNumber2DTO(slug, chapterNumber);
        return ControllerResponse.success("Chapter retrieved successfully", chapter);

    }

    @Operation(summary = "Get chapter by ID", description = "Get detailed information about a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Error retrieving chapter content")
    })
    @GetMapping("/{id}")
    public ControllerResponse<ChapterDetailDTO> getChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        ChapterDetailDTO chapter = chapterService.getChapterDetailDTO(id);
        return ControllerResponse.success("Chapter retrieved successfully", chapter);
    }

    @Operation(summary = "Get chapter audio", description = "Get or generate audio content for a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audio content retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Error generating audio content")
    })
    @GetMapping("/{id}/audio")
    public ControllerResponse<ChapterDetailDTO> createChapterAudio(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        ChapterDetailDTO chapter = chapterService.createChapterAudio(id);
        return ControllerResponse.success("Chapter audio retrieved successfully", chapter);
    }

    @Operation(summary = "Create new chapter", description = "Create a new chapter for a novel", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or error saving content"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    public ControllerResponse<ChapterDTO> createChapter(
            @Parameter(description = "Chapter details", required = true) @RequestBody CreateChapterDTO chapterDTO) {
        ChapterDTO chapter = chapterService.createChapterDTO(chapterDTO);
        return ControllerResponse.success("Chapter created successfully", chapter);
    }

    @Operation(summary = "Update chapter", description = "Update an existing chapter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter updated successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input or error saving content"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{id}")
    public ControllerResponse<ChapterDTO> updateChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated chapter details", required = true) @RequestBody CreateChapterDTO chapterDTO) {

        ChapterDTO chapter = chapterService.updateChapterDTO(id, chapterDTO);
        return ControllerResponse.success("Chapter updated successfully", chapter);

    }

    @Operation(summary = "Delete chapter", description = "Delete an existing chapter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chapter deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @DeleteMapping("/{id}")
    public ControllerResponse<Void> deleteChapter(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ControllerResponse.success("Chapter deleted successfully", null);
    }

    @Operation(summary = "Get chapter JSON file metadata", description = "Retrieve metadata for the JSON file of a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JSON file metadata retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found")
    })
    @GetMapping("/{id}/json-metadata")
    public ControllerResponse<FileMetadata> getChapterJsonMetadata(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
            return ControllerResponse.success(chapterService.getChapterJsonMetadata(id));
    }

    @Operation(summary = "Get chapter audio file metadata", description = "Retrieve metadata for the audio file of a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audio file metadata retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found")
    })
    @GetMapping("/{id}/audio-metadata")
    public ControllerResponse<FileMetadata> getChapterAudioMetadata(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID id) {
            return ControllerResponse.success(chapterService.getChapterAudioMetadata(id));
    }

    @Operation(summary = "Upload a single chapter .txt", description = "Filename must start with the chapter number, e.g. 1.txt. First line is the title; remainder is the content.")
    @ApiResponse(responseCode = "200", description = "Processed")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ControllerResponse<UploadChapterResult> uploadOne(
            @RequestParam("novelId") UUID novelId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("chapterNumber") Integer chapterNumber,
            @RequestParam("title") String title,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        return ControllerResponse
                .success(advancedChapterUploadService.uploadOneAdvanced(novelId, file, chapterNumber, title, overwrite));
    }

    @Operation(summary = "Upload multiple chapter .txt files", description = "Each filename must start with the chapter number (e.g. 1.txt, 2_my-title.txt). First line is title; remainder is content.")
    @ApiResponse(responseCode = "200", description = "Processed")
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ControllerResponse<List<UploadChapterResult>> uploadMany(
            @RequestParam("novelId") UUID novelId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        return ControllerResponse.success(advancedChapterUploadService.uploadManyAdvanced(novelId, files, overwrite));
    }
}
