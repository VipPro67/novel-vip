package com.novel.vippro.controllers;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.mapper.NovelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private NovelMapper novelMapper;

    @GetMapping
    public ResponseEntity<Page<NovelDTO>> getAllNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "views") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Novel> novels = novelRepository.findAll(pageable);
        Page<NovelDTO> novelDTOs = novels.map(novelMapper::toDTO);
        return ResponseEntity.ok(novelDTOs);
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
    public ResponseEntity<NovelDTO> getNovelById(@PathVariable Long id) {
        return novelRepository.findById(id)
                .map(novel -> ResponseEntity.ok(novelMapper.toDTO(novel)))
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

    @GetMapping("/{novelId}/chapters/{chapterId}")
    public ResponseEntity<Chapter> getChapterContent(
            @PathVariable Long novelId,
            @PathVariable Long chapterId) {
        return novelRepository.findById(novelId)
                .map(novel -> novel.getChapters().stream()
                        .filter(chapter -> chapter.getId().equals(chapterId))
                        .findFirst()
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }
}