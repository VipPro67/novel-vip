package com.novel.vippro.controllers;

import com.novel.vippro.models.Novel;
import com.novel.vippro.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelRepository novelRepository;

    @GetMapping
    public ResponseEntity<Page<Novel>> getAllNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "views") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ResponseEntity.ok(novelRepository.findAll(pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Novel>> getNovelsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(novelRepository.findByCategoriesContaining(category, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Novel>> getNovelsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(novelRepository.findByStatus(status, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Novel>> searchNovels(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(novelRepository.searchByKeyword(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Novel> getNovelById(@PathVariable Long id) {
        return novelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hot")
    public ResponseEntity<Page<Novel>> getHotNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(novelRepository.findAllByOrderByViewsDesc(pageable));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<Page<Novel>> getTopRatedNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(novelRepository.findAllByOrderByRatingDesc(pageable));
    }
}