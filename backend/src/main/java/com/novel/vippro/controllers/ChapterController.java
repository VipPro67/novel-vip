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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<ChapterDetailDTO>> getChapterByNumber(
            @PathVariable UUID novelId,
            @PathVariable Integer chapterNumber) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterByNumberDTO(novelId, chapterNumber);
            return ResponseEntity.ok(ApiResponse.success("Chapter retrieved successfully", chapter));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to get chapter content: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChapterDetailDTO>> getChapter(@PathVariable UUID id) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterDetailDTO(id);
            return ResponseEntity.ok(ApiResponse.success("Chapter retrieved successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to get chapter content: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{id}/audio")
    public ResponseEntity<ApiResponse<ChapterDetailDTO>> getChapterAudio(@PathVariable UUID id) {
        try {
            ChapterDetailDTO chapter = chapterService.getChapterAudio(id);
            return ResponseEntity.ok(ApiResponse.success("Chapter audio retrieved successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve chapter audio: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChapterListDTO>> createChapter(@RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.createChapterDTO(chapterDTO);
            return ResponseEntity.ok(ApiResponse.success("Chapter created successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create chapter: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChapterListDTO>> updateChapter(
            @PathVariable UUID id,
            @RequestBody ChapterCreateDTO chapterDTO) {
        try {
            ChapterListDTO chapter = chapterService.updateChapterDTO(id, chapterDTO);
            return ResponseEntity.ok(ApiResponse.success("Chapter updated successfully", chapter));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update chapter: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.ok(ApiResponse.success("Chapter deleted successfully", null));
    }
}