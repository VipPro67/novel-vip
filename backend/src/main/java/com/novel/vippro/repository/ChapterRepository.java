package com.novel.vippro.repository;

import com.novel.vippro.dto.ChapterDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.models.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    @Query("SELECT new com.novel.vippro.dto.ChapterDTO(c, n) FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 ORDER BY c.chapterNumber ASC")
    List<ChapterDTO> findByNovelIdOrderByChapterNumberAsc(UUID novelId, Pageable pageable);

    @Query("SELECT c,n FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 ORDER BY c.chapterNumber ASC")
    List<ChapterDTO> findByNovelIdOrderByChapterNumberAsc(UUID novelId);

    @Query("SELECT new com.novel.vippro.dto.ChapterDetailDTO(c, n) FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 AND c.chapterNumber = ?2 ")
    ChapterDetailDTO findByNovelIdAndChapterNumber(UUID novelId, Integer chapterNumber);

}