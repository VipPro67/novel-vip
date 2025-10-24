package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Favorite;
import com.novel.vippro.Models.Novel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    @Query("SELECT n FROM Favorite f JOIN f.novel n WHERE f.user.id = ?1 ORDER BY f.createdAt DESC")
    Page<Novel> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT f FROM Favorite f WHERE f.novel.id = ?1")
    List<Favorite> findByNovelId(UUID novelId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = ?1 AND f.novel.id = ?2")
    Optional<Favorite> findByUserIdAndNovelId(UUID userId, UUID novelId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.id = ?1 AND f.novel.id = ?2")
    boolean existsByUserIdAndNovelId(UUID userId, UUID novelId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.novel.id = ?1")
    long countByNovelId(UUID novelId);

    @Query("DELETE FROM Favorite f WHERE f.user.id = ?1 AND f.novel.id = ?2")
    void deleteByUserIdAndNovelId(UUID userId, UUID novelId);
}
