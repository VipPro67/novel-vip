package com.novel.vippro.services;

import com.novel.vippro.dto.CommentDTO;
import com.novel.vippro.dto.CommentCreateDTO;
import com.novel.vippro.dto.CommentUpdateDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.Comment;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.CommentRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.repository.ChapterRepository;
import com.novel.vippro.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private Mapper mapper;

    public PageResponse<CommentDTO> getNovelComments(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return new PageResponse<>(commentRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(mapper::CommenttoDTO));
    }

    public PageResponse<CommentDTO> getChapterComments(UUID chapterId, Pageable pageable) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter", "id", chapterId);
        }
        return new PageResponse<>(commentRepository.findByChapterIdOrderByCreatedAtDesc(chapterId, pageable)
                .map(mapper::CommenttoDTO));
    }

    public PageResponse<CommentDTO> getUserComments(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return new PageResponse<>(commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::CommenttoDTO));
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

        return mapper.CommenttoDTO(commentRepository.save(comment));
    }

    @Transactional
    public CommentDTO updateComment(UUID id, CommentUpdateDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Verify ownership or admin rights here if needed

        comment.setContent(commentDTO.getContent());
        return mapper.CommenttoDTO(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        // Verify ownership or admin rights here if needed

        commentRepository.delete(comment);
    }

    public PageResponse<CommentDTO> getCommentReplies(UUID commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        return new PageResponse<>(commentRepository.findByParentIdOrderByCreatedAtAsc(commentId, pageable)
                .map(mapper::CommenttoDTO));
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

        return mapper.CommenttoDTO(commentRepository.save(reply));
    }

}