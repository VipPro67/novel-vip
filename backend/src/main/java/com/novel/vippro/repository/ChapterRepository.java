package com.novel.vippro.repository;

import com.novel.vippro.models.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    @Query("SELECT c FROM Chapter c WHERE c.novel.id = ?1 ORDER BY c.chapterNumber ASC")
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    @Query("SELECT c FROM Chapter c WHERE c.novel.id = ?1 AND c.chapterNumber = ?2")
    Chapter findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    @Query("SELECT c FROM Chapter c WHERE c.novel.id = ?1")
    Page<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId, Pageable pageable);
}