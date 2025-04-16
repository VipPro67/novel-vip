package com.novel.vippro.services;

import com.novel.vippro.dto.CommentDTO;
import com.novel.vippro.dto.CommentCreateDTO;
import com.novel.vippro.dto.CommentUpdateDTO;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

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

    public Page<CommentDTO> getNovelComments(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return commentRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(this::convertToDTO);
    }

    public Page<CommentDTO> getChapterComments(UUID chapterId, Pageable pageable) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter", "id", chapterId);
        }
        return commentRepository.findByChapterIdOrderByCreatedAtDesc(chapterId, pageable)
                .map(this::convertToDTO);
    }

    public Page<CommentDTO> getUserComments(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public CommentDTO addComment(CommentCreateDTO commentDTO) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        comment.setUser(user);

        // Set novel if provided
        if (commentDTO.getNovelId() != null) {
            Novel novel = novelRepository.findById(commentDTO.getNovelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", commentDTO.getNovelId()));
            comment.setNovel(novel);
        }

        // Set chapter if provided
        if (commentDTO.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(commentDTO.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", commentDTO.getChapterId()));
            comment.setChapter(chapter);
        }

        return convertToDTO(commentRepository.save(comment));
    }

    @Transactional
    public CommentDTO updateComment(UUID id, CommentUpdateDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Verify ownership or admin rights here if needed

        comment.setContent(commentDTO.getContent());
        return convertToDTO(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Verify ownership or admin rights here if needed

        commentRepository.delete(comment);
    }

    public Page<CommentDTO> getCommentReplies(UUID commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        return commentRepository.findByParentIdOrderByCreatedAtAsc(commentId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public CommentDTO addReply(UUID parentId, CommentCreateDTO replyDTO) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentId));

        Comment reply = new Comment();
        reply.setContent(replyDTO.getContent());
        reply.setParent(parentComment);
        reply.setNovel(parentComment.getNovel());
        reply.setChapter(parentComment.getChapter());

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        reply.setUser(user);

        return convertToDTO(commentRepository.save(reply));
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());

        if (comment.getNovel() != null) {
            dto.setNovelId(comment.getNovel().getId());
        }

        if (comment.getChapter() != null) {
            dto.setChapterId(comment.getChapter().getId());
        }

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }

        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}