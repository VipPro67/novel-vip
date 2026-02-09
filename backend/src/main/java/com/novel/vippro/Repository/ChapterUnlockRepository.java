package com.novel.vippro.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.ChapterUnlock;

@Repository
public interface ChapterUnlockRepository extends JpaRepository<ChapterUnlock, UUID> {
    boolean existsByUserIdAndChapterId(UUID userId, UUID chapterId);
}
