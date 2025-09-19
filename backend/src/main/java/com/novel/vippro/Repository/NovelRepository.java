package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Novel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface NovelRepository extends JpaRepository<Novel, UUID> {

    @Query("SELECT n.id FROM Novel n")
    Page<UUID> findAllIds(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.id IN :ids")
    List<Novel> findAllByIdInWithGraph(@Param("ids") List<UUID> ids);

    @Query("SELECT n FROM Novel n WHERE n.id = :id")
    @EntityGraph(attributePaths = {"categories", "tags", "genres", "owner","coverImage"})
    Optional<Novel> findById(@Param("id") UUID id);

    @Query("SELECT n FROM Novel n WHERE n.slug = :slug")
    @EntityGraph(attributePaths = {"categories", "tags", "genres", "owner","coverImage"})
    Novel findBySlugWithGraph(@Param("slug") String slug);

    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE c.name = :category")
    Page<Novel> findByCategoriesContaining(String category, Pageable pageable);

    @Query("SELECT n FROM Novel n JOIN n.genres c WHERE c.id = :genreId")
    Page<Novel> findByGenresId(@Param("genreId") UUID genreId, Pageable pageable);

    @Query("SELECT n FROM Novel n JOIN n.tags c WHERE c.id = :tagId")
    Page<Novel> findByTagsId(@Param("tagId") UUID tagId, Pageable pageable);

    @Query("SELECT n FROM Novel n JOIN n.categories c WHERE c.id = :categoryId")
    Page<Novel> findByCategoriesId(@Param("categoryId") UUID categoryId, Pageable pageable);
    
    @Query("SELECT n FROM Novel n WHERE n.status = :status")
    Page<Novel> findByStatus(String status, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.titleNormalized LIKE %:keyword%")
    Page<Novel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT DISTINCT n FROM Novel n
            LEFT JOIN n.categories c
            LEFT JOIN n.genres g
            LEFT JOIN n.tags t
            WHERE (:keyword IS NULL OR 
                  CAST(n.titleNormalized AS string) ILIKE CONCAT('%', CAST(:keyword AS string), '%')
                  OR CAST(n.description AS string) ILIKE CONCAT('%', CAST(:keyword AS string), '%')
                  OR CAST(n.author AS string) ILIKE CONCAT('%', CAST(:keyword AS string), '%'))
              AND (:title IS NULL OR CAST(n.title AS string) ILIKE CONCAT('%', CAST(:title AS string), '%'))
              AND (:author IS NULL OR CAST(n.author AS string) ILIKE CONCAT('%', CAST(:author AS string), '%'))
              AND (:category IS NULL OR CAST(c.name AS string) ILIKE CONCAT('%', CAST(:category AS string), '%'))
              AND (:tag IS NULL OR CAST(t.name AS string) ILIKE CONCAT('%', CAST(:tag AS string), '%'))
              AND (:genre IS NULL OR CAST(g.name AS string) ILIKE CONCAT('%', CAST(:genre AS string), '%'))
            """)
    Page<Novel> searchByCriteria(
            @Param("keyword") String keyword,
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category,
            @Param("genre") String genre,
            @Param("tag") String tag,
            Pageable pageable);

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

    @Query("SELECT n FROM Novel n ORDER BY n.updatedAt DESC")
    Page<Novel> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.slug = :slug")
    Optional<Novel> findBySlug(String slug);
}