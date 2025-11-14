package com.novel.vippro.Services;

import com.novel.vippro.DTO.Comment.CommentCreateDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Comment.CommentUpdateDTO;
import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Comment;
import com.novel.vippro.Models.NotificationType;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.CommentRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Messaging.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.novel.vippro.Security.UserDetailsImpl;

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

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Transactional(readOnly = true)
    public PageResponse<CommentDTO> getNovelComments(UUID novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return new PageResponse<>(commentRepository.findByNovelIdOrderByCreatedAtDesc(novelId, pageable)
                .map(mapper::CommenttoDTO));
    }

    @Transactional(readOnly = true)
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
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        comment.setUser(user);

        if (commentDTO.getNovelId() != null) {
            Novel novel = novelRepository.findById(commentDTO.getNovelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", commentDTO.getNovelId()));
            comment.setNovel(novel);
        }

        if (commentDTO.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(commentDTO.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", commentDTO.getChapterId()));
            comment.setChapter(chapter);
        }

        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentDTO.getParentId()));
            comment.setParent(parentComment);
            if (!parentComment.getUser().getId().equals(user.getId())) {
                CreateNotificationDTO notificationDTO = CreateNotificationDTO.builder()
                        .userId(parentComment.getUser().getId())
                        .title(user.getUsername() + " replied to your comment")
                        .message("You have a new reply on your comment: " + parentComment.getContent() + " at " +
                                (comment.getNovel() != null ? "Novel: " + comment.getNovel().getTitle()
                                        : "Chapter: " + comment.getChapter().getTitle()))
                        .type(NotificationType.COMMENT)
                        .build();
                notificationService.createNotification(notificationDTO);

            }
        }
        logger.info(comment.toString()
        );
        Comment saved = commentRepository.save(comment);
        CommentDTO dto = mapper.CommenttoDTO(saved);

        messagePublisher.publishComment(dto);
        return dto;
    }

    @Transactional
    public CommentDTO updateComment(UUID id, CommentUpdateDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to edit this comment");
        }

        comment.setContent(commentDTO.getContent());
        return mapper.CommenttoDTO(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    public PageResponse<CommentDTO> getCommentReplies(UUID commentId, Pageable pageable) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        return new PageResponse<>(commentRepository.findByParentIdOrderByCreatedAtAsc(commentId, pageable)
                .map(mapper::CommenttoDTO));
    }

}
