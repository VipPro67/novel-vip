package com.novel.vippro.services;

import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.models.Comment;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.CommentRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.repository.ChapterRepository;
import com.novel.vippro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<Comment> getCommentsByNovel(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return commentRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable);
    }

    public Page<Comment> getCommentsByChapter(UUID chapterId, Pageable pageable) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter", "id", chapterId);
        }
        return commentRepository.findByChapterIdOrderByCreatedAtDesc(chapterId, pageable);
    }

    public Page<Comment> getCommentsByUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    public Comment createComment(UUID userId, UUID novelId, UUID chapterId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);

        // Set novel if provided
        if (novelId != null) {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));
            comment.setNovel(novel);
        }

        // Set chapter if provided
        if (chapterId != null) {
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
            comment.setChapter(chapter);
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(UUID id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        commentRepository.delete(comment);
    }
}