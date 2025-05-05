package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Bookmark;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    @Query("SELECT b FROM Bookmark b WHERE b.user.id = ?1 ORDER BY b.updatedAt DESC")
    Page<Bookmark> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT b FROM Bookmark b WHERE b.novel.id = ?1 ORDER BY b.updatedAt DESC")
    List<Bookmark> findByNovelId(UUID novelId);

    @Query("SELECT b FROM Bookmark b WHERE b.chapter.id = ?1 ORDER BY b.updatedAt DESC")
    List<Bookmark> findByChapterId(UUID chapterId);

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = ?1 AND b.chapter.id = ?2")
    Bookmark findByUserIdAndChapterId(UUID userId, UUID chapterId);

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = ?1 AND b.novel.id = ?2")
    List<Bookmark> findByUserIdAndNovelId(UUID userId, UUID novelId);
}