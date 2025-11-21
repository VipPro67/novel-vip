package com.novel.vippro.Repository;

import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.ChapterUnlock;
import com.novel.vippro.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChapterUnlockRepository extends JpaRepository<ChapterUnlock, UUID> {
    Optional<ChapterUnlock> findByUserAndChapter(User user, Chapter chapter);
    boolean existsByUserAndChapter(User user, Chapter chapter);
}
