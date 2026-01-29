package com.novel.vippro.Repository;

import com.novel.vippro.Models.CultivationRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CultivationRankRepository extends JpaRepository<CultivationRank, UUID> {
    Optional<CultivationRank> findByRankLevel(Integer rankLevel);
    
    // Find the highest rank that requires less than or equal to given points
    Optional<CultivationRank> findTopByRequiredPointsLessThanEqualOrderByRankLevelDesc(Long points);
}
