package com.novel.vippro.Repository;

import com.novel.vippro.Models.ChapterAudioLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChapterAudioLogRepository extends JpaRepository<ChapterAudioLog, UUID> {
}
