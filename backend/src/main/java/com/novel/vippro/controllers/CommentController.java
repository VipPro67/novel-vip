package com.novel.vippro.controllers;

import com.novel.vippro.models.Comment;
import com.novel.vippro.payload.response.ApiResponse;
import com.novel.vippro.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/novel/{novelId}")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByNovel(
            @PathVariable UUID novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getCommentsByNovel(novelId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByChapter(
            @PathVariable UUID chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getCommentsByChapter(chapterId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Comment>> createComment(
            @RequestParam UUID userId,
            @RequestParam(required = false) UUID novelId,
            @RequestParam(required = false) UUID chapterId,
            @RequestParam String content) {
        Comment comment = commentService.createComment(userId, novelId, chapterId, content);
        return ResponseEntity.ok(ApiResponse.success("Comment created successfully", comment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Comment>> updateComment(
            @PathVariable UUID id,
            @RequestParam String content) {
        Comment comment = commentService.updateComment(id, content);
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", comment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}