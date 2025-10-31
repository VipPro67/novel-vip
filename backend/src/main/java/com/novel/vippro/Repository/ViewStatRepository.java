package com.novel.vippro.Repository;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.ViewStat;

@Repository
public interface ViewStatRepository extends JpaRepository<ViewStat, UUID> {
    @Query("SELECT COUNT(v) FROM ViewStat v WHERE v.novel.id = :novelId")
    Long countTotalViewsByNovelId(@Param("novelId") UUID novelId);
    
    @Query("SELECT COUNT(v) FROM ViewStat v WHERE v.novel.id = :novelId AND v.viewDate >= :startDate")
    Long countViewsByNovelIdAndDateAfter(
        @Param("novelId") UUID novelId, 
        @Param("startDate") LocalDateTime startDate
    );

    @Query("SELECT COUNT(v) FROM ViewStat v WHERE v.novel.id = :novelId AND v.user.id = :userId AND v.viewDate >= :lastTime")
    Long countRecentViewsByUserAndNovel(
        @Param("novelId") UUID novelId,
        @Param("userId") UUID userId,
        @Param("lastTime") LocalDateTime lastTime
    );
}