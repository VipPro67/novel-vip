package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Favorite;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndNovelId(UUID userId, UUID novelId);

    boolean existsByUserIdAndNovelId(UUID userId, UUID novelId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.novel.id = ?1")
    long countByNovelId(UUID novelId);

    void deleteByUserIdAndNovelId(UUID userId, UUID novelId);
}