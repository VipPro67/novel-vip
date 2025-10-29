package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ReadingHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, UUID> {
        
        @Query("SELECT h FROM ReadingHistory h LEFT JOIN FETCH h.novel n LEFT JOIN FETCH n.coverImage LEFT JOIN FETCH h.chapter WHERE h.user.id = :userId ORDER BY h.lastReadAt DESC")
        Page<ReadingHistory> findByUserIdOrderByLastReadAtDesc(UUID userId, Pageable pageable);

        @Query("SELECT h FROM ReadingHistory h LEFT JOIN FETCH h.novel n LEFT JOIN FETCH n.coverImage LEFT JOIN FETCH h.chapter WHERE h.novel.id = :novelId ORDER BY h.lastReadAt DESC")
        Page<ReadingHistory> findByNovelIdOrderByLastReadAtDesc(@Param("novelId") UUID novelId, Pageable pageable);

        @Query("SELECT h FROM ReadingHistory h LEFT JOIN FETCH h.novel n LEFT JOIN FETCH n.coverImage LEFT JOIN FETCH h.chapter WHERE h.user.id = :userId AND h.novel.id = :novelId ORDER BY h.lastReadAt DESC")
        Optional<ReadingHistory> findFirstByUserIdAndNovelIdOrderByLastReadAtDesc(@Param("userId") UUID userId, @Param("novelId") UUID novelId);

        @Query("SELECT COUNT(DISTINCT h.chapter.id) FROM ReadingHistory h WHERE h.user.id = :userId")
        long countTotalChaptersRead(@Param("userId") UUID userId);

        @Query("SELECT COUNT(DISTINCT h.novel.id) FROM ReadingHistory h WHERE h.user.id = :userId")
        long countTotalNovelsRead(@Param("userId") UUID userId);

        @Query("SELECT c.name, COUNT(h) as count FROM ReadingHistory h " +
                        "JOIN h.novel n JOIN n.categories c " +
                        "WHERE h.user.id = :userId " +
                        "GROUP BY c.name ORDER BY count DESC")
        List<Object[]> findMostReadGenre(@Param("userId") UUID userId);

        @Query("SELECT n.author, COUNT(h) as count FROM ReadingHistory h " +
                        "JOIN h.novel n WHERE h.user.id = :userId " +
                        "GROUP BY n.author ORDER BY count DESC")
        List<Object[]> findFavoriteAuthor(@Param("userId") UUID userId);

        @Query("SELECT h FROM ReadingHistory h WHERE h.user.id = :userId AND h.novel.id = :novelId " +
                        "AND h.chapter.id = :chapterId")
        Optional<ReadingHistory> findByUserIdAndNovelIdAndChapterId(
                        @Param("userId") UUID userId,
                        @Param("novelId") UUID novelId,
                        @Param("chapterId") UUID chapterId);

        void deleteByUserId(UUID userId);
}