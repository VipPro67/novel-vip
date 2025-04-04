package com.novel.vippro.repository;

import com.novel.vippro.models.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    Chapter findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    Page<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId, Pageable pageable);
}