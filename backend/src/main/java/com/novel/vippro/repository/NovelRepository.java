package com.novel.vippro.repository;

import com.novel.vippro.models.Novel;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NovelRepository extends JpaRepository<Novel, Long> {

    @Query("SELECT n FROM Novel n WHERE n.id = :id")
    Optional<Novel> findById(@Param("id") long id);

    @Query("SELECT n FROM Novel n WHERE n.categories LIKE %:category%")
    Page<Novel> findByCategoriesContaining(String category, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.status = :status")
    Page<Novel> findByStatus(String status, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.title LIKE %:keyword% OR n.author LIKE %:keyword%")
    Page<Novel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.views > 0 ORDER BY n.views DESC")
    Page<Novel> findAllByOrderByViewsDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.views > 0 ORDER BY n.views DESC")
    Page<Novel> findAllByOrderByRatingDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.author LIKE %:author%")
    Page<Novel> findByAuthorContaining(String author, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.title LIKE %:title%")
    Page<Novel> findByTitleContaining(String title, Pageable pageable);
}