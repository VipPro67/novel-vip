package com.novel.vippro.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.ReadingHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, UUID> {
        Page<ReadingHistory> findByUserIdOrderByLastReadAtDesc(UUID userId, Pageable pageable);

        Page<ReadingHistory> findByNovelIdOrderByLastReadAtDesc(UUID novelId, Pageable pageable);

        Optional<ReadingHistory> findFirstByUserIdAndNovelIdOrderByLastReadAtDesc(UUID userId, UUID novelId);

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

        @Query("SELECT h FROM ReadingHistory h WHERE h.user.id = :userId ORDER BY h.lastReadAt DESC")
        List<ReadingHistory> findRecentlyReadNovels(@Param("userId") UUID userId, Pageable pageable);
}