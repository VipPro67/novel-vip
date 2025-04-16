package com.novel.vippro.repository;

import com.novel.vippro.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c FROM Comment c WHERE c.novel.id = ?1 AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByNovelIdOrderByCreatedAtDesc(UUID novelId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.chapter.id = ?1 AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByChapterIdOrderByCreatedAtDesc(UUID chapterId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.user.id = ?1 ORDER BY c.createdAt DESC")
    Page<Comment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = ?1 ORDER BY c.createdAt ASC")
    Page<Comment> findByParentIdOrderByCreatedAtAsc(UUID parentId, Pageable pageable);
}