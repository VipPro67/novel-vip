package com.novel.vippro.Repository;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.Models.Chapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    @Query("SELECT c FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 ORDER BY c.chapterNumber ASC")
    Page<Chapter> findByNovelIdOrderByChapterNumberAsc(UUID novelId, Pageable pageable);

    @Query("SELECT c,n FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 ORDER BY c.chapterNumber ASC")
    List<ChapterDTO> findByNovelIdOrderByChapterNumberAsc(UUID novelId);

    @Query("SELECT c, n FROM Chapter c JOIN c.novel n WHERE c.novel.id = ?1 AND c.chapterNumber = ?2 ")
    ChapterDetailDTO findByNovelIdAndChapterNumber(UUID novelId, Integer chapterNumber);

}