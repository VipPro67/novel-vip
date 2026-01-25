package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Comment.CommentCreateDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Comment.CommentUpdateDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.CommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Novel and chapter comments management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "Get novel comments", description = "Get all comments for a specific novel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/novel/{novelId}")
    public ControllerResponse<PageResponse<CommentDTO>> getNovelComments(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<CommentDTO> comments = commentService.getNovelComments(novelId, pageable);
        return ControllerResponse.success("Comments retrieved successfully", comments);
    }

    @Operation(summary = "Get chapter comments", description = "Get all comments for a specific chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chapter not found")
    })
    @GetMapping("/chapter/{chapterId}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<PageResponse<CommentDTO>> getChapterComments(
            @Parameter(description = "Chapter ID", required = true) @PathVariable UUID chapterId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<CommentDTO> comments = commentService.getChapterComments(chapterId, pageable);
        return ControllerResponse.success("Comments retrieved successfully", comments);
    }

    @Operation(summary = "Get user comments", description = "Get all comments made by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<PageResponse<CommentDTO>> getUserComments(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<CommentDTO> comments = commentService.getUserComments(userId, pageable);
        return ControllerResponse.success("Comments retrieved successfully", comments);
    }

    @Operation(summary = "Add comment", description = "Add a new comment to a novel or chapter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Novel or chapter not found")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<CommentDTO> addComment(
            @Parameter(description = "Comment details", required = true) @Valid @RequestBody CommentCreateDTO commentDTO) {
        CommentDTO createdComment = commentService.addComment(commentDTO);
        return ControllerResponse.success("Comment added successfully", createdComment);
    }

    @Operation(summary = "Update comment", description = "Update an existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<CommentDTO> updateComment(
            @Parameter(description = "Comment ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated comment details", required = true) @Valid @RequestBody CommentUpdateDTO commentDTO) {
        CommentDTO updatedComment = commentService.updateComment(id, commentDTO);
        return ControllerResponse.success("Comment updated successfully", updatedComment);
    }

    @Operation(summary = "Delete comment", description = "Delete an existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> deleteComment(
            @Parameter(description = "Comment ID", required = true) @PathVariable UUID id) {
        commentService.deleteComment(id);
        return ControllerResponse.success("Comment deleted successfully", null);
    }

    @Operation(summary = "Get comment replies", description = "Get all replies to a specific comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parent comment not found")
    })
    @GetMapping("/{commentId}/replies")
        @PreAuthorize("isAuthenticated()")
    public ControllerResponse<PageResponse<CommentDTO>> getReplies(
            @Parameter(description = "Parent comment ID", required = true) @PathVariable UUID commentId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<CommentDTO> replies = commentService.getCommentReplies(commentId, pageable);
        return ControllerResponse.success("Replies retrieved successfully", replies);
    }
}