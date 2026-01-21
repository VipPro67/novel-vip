package com.novel.vippro.Repository;

import com.novel.vippro.Models.NovelSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NovelSourceRepository extends JpaRepository<NovelSource, UUID> {
    
    Optional<NovelSource> findByNovelIdAndSourceUrl(UUID novelId, String sourceUrl);
    
    List<NovelSource> findByNovelId(UUID novelId);
    
    @Query("SELECT ns FROM NovelSource ns WHERE ns.enabled = true AND ns.nextSyncTime <= :time")
    List<NovelSource> findDueForSync(@Param("time") Instant time);
    
    @Query("SELECT ns FROM NovelSource ns WHERE ns.enabled = true AND ns.sourcePlatform = :platform")
    List<NovelSource> findByEnabledAndSourcePlatform(@Param("platform") String platform);
    
    boolean existsByNovelIdAndSourceUrl(UUID novelId, String sourceUrl);
    
    long countByNovelId(UUID novelId);
}
