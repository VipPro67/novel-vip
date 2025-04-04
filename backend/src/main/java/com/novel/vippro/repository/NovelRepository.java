package com.novel.vippro.repository;

import com.novel.vippro.models.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NovelRepository extends JpaRepository<Novel, Long> {
    Page<Novel> findByCategoriesContaining(String category, Pageable pageable);

    Page<Novel> findByStatus(String status, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.title LIKE %:keyword% OR n.author LIKE %:keyword%")
    Page<Novel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Novel> findAllByOrderByViewsDesc(Pageable pageable);

    Page<Novel> findAllByOrderByRatingDesc(Pageable pageable);
}