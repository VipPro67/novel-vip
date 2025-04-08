package com.novel.vippro.controllers;

import com.novel.vippro.models.Chapter;
import com.novel.vippro.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterRepository chapterRepository;

    @GetMapping("/novel/{novelId}")
    public ResponseEntity<Page<Chapter>> getChaptersByNovel(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable));
    }

    @GetMapping("/novel/{novelId}/chapter/{chapterNumber}")
    public ResponseEntity<Chapter> getChapterByNumber(
            @PathVariable Long novelId,
            @PathVariable Integer chapterNumber) {
        return ResponseEntity.ok(chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chapter> getChapterById(@PathVariable Long id) {
        return chapterRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}