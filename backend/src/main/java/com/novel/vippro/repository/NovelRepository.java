package com.novel.vippro.repository;

import com.novel.vippro.models.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface NovelRepository extends JpaRepository<Novel, UUID> {

    @Query("SELECT n FROM Novel n WHERE n.id = :id")
    Optional<Novel> findById(@Param("id") UUID id);

    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE c.name = :category")
    Page<Novel> findByCategoriesContaining(String category, Pageable pageable);

    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE c.id = :categoryId")
    Page<Novel> findByCategoriesId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.status = :status")
    Page<Novel> findByStatus(String status, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.titleNomalized LIKE %:keyword%")
    Page<Novel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.views > 0 ORDER BY n.views DESC")
    Page<Novel> findAllByOrderByViewsDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.views > 0 ORDER BY n.views DESC")
    Page<Novel> findAllByOrderByRatingDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.author LIKE %:author%")
    Page<Novel> findByAuthorContaining(String author, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.title LIKE %:title%")
    Page<Novel> findByTitleContaining(String title, Pageable pageable);

    // For recommendations
    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE n.id NOT IN :excludeIds AND c.name = :categoryName ORDER BY n.rating DESC")
    List<Novel> findByCategoryNameAndIdNotIn(String categoryName, Set<UUID> excludeIds, Pageable pageable);

    List<Novel> findByAuthorAndIdNotIn(String author, Set<UUID> excludeIds, Pageable pageable);

    List<Novel> findByIdNotInOrderByRatingDesc(Set<UUID> excludeIds, Pageable pageable);

    // Additional novel search methods
    Page<Novel> findByTitleContainingIgnoreCaseOrderByRatingDesc(String title, Pageable pageable);

    Page<Novel> findByAuthorContainingIgnoreCaseOrderByRatingDesc(String author, Pageable pageable);

    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE c.name LIKE %:category%")
    Page<Novel> findByCategoryNameContainingIgnoreCaseOrderByRatingDesc(String category, Pageable pageable);

    List<Novel> findByGenresInAndTagsIn(ArrayList<String> genres, ArrayList<String> tags);

    @Query("SELECT n FROM Novel n WHERE n.rating >= :minRating ORDER BY n.rating DESC")
    Page<Novel> findByMinimumRating(int minRating, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.views >= :minViews ORDER BY n.views DESC")
    Page<Novel> findPopularNovels(int minViews, Pageable pageable);

    // @Query("SELECT n FROM Novel n WHERE n.createdAt >= CURRENT_DATE - 7 ORDER BY
    // n.rating DESC")
    // Page<Novel> findRecentlyAddedNovels(Pageable pageable);

    // Added methods for RecommendationService
    @Query("SELECT n FROM Novel n ORDER BY n.rating DESC")
    Page<Novel> findAllByOrderByAverageRatingDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n ORDER BY n.createdAt DESC")
    Page<Novel> findAllByOrderByCreatedAtDesc(Pageable pageable);
}