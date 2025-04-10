package com.novel.vippro.controllers;

import com.novel.vippro.dto.ChapterCreateDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.dto.ChapterListDTO;
import com.novel.vippro.payload.response.ApiResponse;
import com.novel.vippro.services.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @GetMapping("/novel/{novelId}")
    public ResponseEntity<ApiResponse<Page<ChapterListDTO>>> getChaptersByNovel(
            @PathVariable UUID novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChapterListDTO> chapters = chapterService.getChaptersByNovelDTO(novelId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Chapters retrieved successfully", chapters));
    }

    @GetMapping("/novel/{novelId}/chapter/{chapterNumber}")
    public ResponseEntity<ApiResponse<ChapterListDTO>> getChapterByNumber(
            @PathVariable UUID novelId,
            @PathVariable Integer chapterNumber) {
        ChapterListDTO chapter = chapterService.getChapterByNumberDTO(novelId, chapterNumber);
        return ResponseEntity.ok(ApiResponse.success("Chapter retrieved successfully", chapter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChapterDetailDTO>> getChapter(@PathVariable UUID id) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterDetailDTO(id);
            return ResponseEntity.ok(ApiResponse.success("Chapter retrieved successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get chapter content: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChapterListDTO>> createChapter(@RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.createChapterDTO(chapterDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chapter created successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to create chapter: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChapterListDTO>> updateChapter(
            @PathVariable UUID id,
            @RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.updateChapterDTO(id, chapterDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chapter updated successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to update chapter: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Chapter deleted successfully", null));
    }
}