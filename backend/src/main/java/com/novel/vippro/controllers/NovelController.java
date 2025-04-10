package com.novel.vippro.controllers;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.models.Comment;
import com.novel.vippro.payload.response.ApiResponse;
import com.novel.vippro.services.NovelService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> getAllNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "views") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<NovelDTO> novels = novelService.getAllNovels(pageable);
        return ResponseEntity.ok(ApiResponse.success("Novels retrieved successfully", novels));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> getNovelsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getNovelsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Novels retrieved successfully", novels));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> getNovelsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getNovelsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Novels retrieved successfully", novels));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> searchNovels(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.searchNovels(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Novels retrieved successfully", novels));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NovelDTO>> getNovelById(@PathVariable UUID id) {
        NovelDTO novel = novelService.getNovelById(id);
        return ResponseEntity.ok(ApiResponse.success("Novel retrieved successfully", novel));
    }

    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> getHotNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getHotNovels(pageable);
        return ResponseEntity.ok(ApiResponse.success("Hot novels retrieved successfully", novels));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<NovelDTO>>> getTopRatedNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NovelDTO> novels = novelService.getTopRatedNovels(pageable);
        return ResponseEntity.ok(ApiResponse.success("Top rated novels retrieved successfully", novels));
    }

    @GetMapping("/{novelId}/chapters")
    public ResponseEntity<ApiResponse<Page<Chapter>>> getChaptersByNovel(
            @PathVariable UUID novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Chapter> chapters = chapterService.getChaptersByNovel(novelId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Chapters retrieved successfully", chapters));
    }

    @GetMapping("/{novelId}/comments")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByNovel(
            @PathVariable UUID novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getCommentsByNovel(novelId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NovelDTO>> createNovel(@RequestBody Novel novel) {
        NovelDTO createdNovel = novelService.createNovel(novel);
        return ResponseEntity.ok(ApiResponse.success("Novel created successfully", createdNovel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NovelDTO>> updateNovel(
            @PathVariable UUID id,
            @RequestBody Novel novel) {
        NovelDTO updatedNovel = novelService.updateNovel(id, novel);
        return ResponseEntity.ok(ApiResponse.success("Novel updated successfully", updatedNovel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNovel(@PathVariable UUID id) {
        novelService.deleteNovel(id);
        return ResponseEntity.ok(ApiResponse.success("Novel deleted successfully", null));
    }

    @PutMapping("/{id}/increment-views")
    public ResponseEntity<ApiResponse<NovelDTO>> incrementViews(@PathVariable UUID id) {
        NovelDTO updatedNovel = novelService.incrementViews(id);
        return ResponseEntity.ok(ApiResponse.success("Novel views incremented successfully", updatedNovel));
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<NovelDTO>> updateRating(
            @PathVariable UUID id,
            @RequestParam int rating) {
        NovelDTO updatedNovel = novelService.updateRating(id, rating);
        return ResponseEntity.ok(ApiResponse.success("Novel rating updated successfully", updatedNovel));
    }
}