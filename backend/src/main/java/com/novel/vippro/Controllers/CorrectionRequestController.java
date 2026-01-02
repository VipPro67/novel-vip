package com.novel.vippro.Controllers;

import com.novel.vippro.Models.CorrectionRequest;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.CorrectionRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/corrections")
@Tag(name = "Text Corrections", description = "Crowdsourced text correction system")
@SecurityRequirement(name = "bearerAuth")
public class CorrectionRequestController {

    private static final Logger logger = LogManager.getLogger(CorrectionRequestController.class);

    @Autowired
    private CorrectionRequestService correctionRequestService;

    /**
     * Submit a new text correction request
     */
    @Operation(summary = "Submit a correction request", description = "Submit a new text correction for a paragraph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Correction request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ControllerResponse<CorrectionRequest>> submitCorrectionRequest(
            @RequestBody SubmitCorrectionRequestDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            UUID userId = userDetails.getId();

            CorrectionRequest correction = correctionRequestService.submitCorrectionRequest(
                    userId,
                    request.getNovelId(),
                    request.getChapterId(),
                    request.getS3Key(),
                    request.getParagraphIndex(),
                    request.getOriginalText(),
                    request.getSuggestedText()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ControllerResponse<>(true, "Correction request submitted successfully", correction));
        } catch (Exception e) {
            logger.error("Error submitting correction request", e);
            return ResponseEntity.badRequest()
                    .body(new ControllerResponse<>(false, "Failed to submit correction: " + e.getMessage()));
        }
    }

    /**
     * Get all pending corrections (Admin only)
     */
    @Operation(summary = "Get pending corrections", description = "Retrieve all pending correction requests (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Corrections retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<PageResponse<CorrectionRequest>>> getPendingCorrections(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
            PageResponse<CorrectionRequest> corrections = correctionRequestService.getPendingCorrections(pageable);
            return ResponseEntity.ok(new ControllerResponse<>(true, "Pending corrections retrieved", corrections));
        } catch (Exception e) {
            logger.error("Error retrieving pending corrections", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ControllerResponse<>(false, "Failed to retrieve corrections: " + e.getMessage()));
        }
    }

    /**
     * Get corrections by status (Admin only)
     */
    @Operation(summary = "Get corrections by status", description = "Retrieve corrections filtered by status")
    @GetMapping("/admin/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<PageResponse<CorrectionRequest>>> getCorrectionsByStatus(
            @Parameter(description = "Correction status") @PathVariable String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            CorrectionRequest.CorrectionStatus correctionStatus = CorrectionRequest.CorrectionStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            PageResponse<CorrectionRequest> corrections = correctionRequestService.getCorrectionsByStatus(correctionStatus, pageable);
            return ResponseEntity.ok(new ControllerResponse<>(true, "Corrections retrieved", corrections));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ControllerResponse<>(false, "Invalid status: " + status));
        } catch (Exception e) {
            logger.error("Error retrieving corrections by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ControllerResponse<>(false, "Failed to retrieve corrections: " + e.getMessage()));
        }
    }

    /**
     * Get a single correction request
     */
    @Operation(summary = "Get correction details", description = "Retrieve details of a specific correction request")
    @GetMapping("/{id}")
    public ResponseEntity<ControllerResponse<CorrectionRequest>> getCorrectionById(
            @Parameter(description = "Correction ID") @PathVariable UUID id) {
        try {
            CorrectionRequest correction = correctionRequestService.getCorrectionById(id);
            return ResponseEntity.ok(new ControllerResponse<>(true, "Correction retrieved", correction));
        } catch (Exception e) {
            logger.error("Error retrieving correction", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ControllerResponse<>(false, "Correction not found: " + e.getMessage()));
        }
    }

    /**
     * Approve a correction request (Admin only) - CRITICAL ENDPOINT
     */
    @Operation(summary = "Approve correction", description = "Approve a pending correction and patch the S3 file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correction approved successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot approve non-pending correction"),
            @ApiResponse(responseCode = "404", description = "Correction not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<CorrectionRequest>> approveCorrectionRequest(
            @Parameter(description = "Correction ID") @PathVariable UUID id) {
        try {
            CorrectionRequest approved = correctionRequestService.approveCorrectionRequest(id);
            return ResponseEntity.ok(new ControllerResponse<>(true, "Correction approved and S3 file patched", approved));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ControllerResponse<>(false, e.getMessage()));
        } catch (IOException e) {
            logger.error("S3 operation failed while approving correction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ControllerResponse<>(false, "Failed to patch S3 file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving correction", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ControllerResponse<>(false, "Correction not found: " + e.getMessage()));
        }
    }

    /**
     * Reject a correction request (Admin only)
     */
    @Operation(summary = "Reject correction", description = "Reject a pending correction request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correction rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot reject non-pending correction"),
            @ApiResponse(responseCode = "404", description = "Correction not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<CorrectionRequest>> rejectCorrectionRequest(
            @Parameter(description = "Correction ID") @PathVariable UUID id,
            @RequestBody RejectCorrectionDTO request) {
        try {
            CorrectionRequest rejected = correctionRequestService.rejectCorrectionRequest(id, request.getReason());
            return ResponseEntity.ok(new ControllerResponse<>(true, "Correction rejected", rejected));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ControllerResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting correction", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ControllerResponse<>(false, "Correction not found: " + e.getMessage()));
        }
    }

    /**
     * Get user's correction history
     */
    @Operation(summary = "Get user's corrections", description = "Retrieve correction requests submitted by the current user")
    @GetMapping("/my-corrections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ControllerResponse<PageResponse<CorrectionRequest>>> getUserCorrections(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            UUID userId = userDetails.getId();

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            PageResponse<CorrectionRequest> corrections = correctionRequestService.getUserCorrections(userId, pageable);
            return ResponseEntity.ok(new ControllerResponse<>(true, "User corrections retrieved", corrections));
        } catch (Exception e) {
            logger.error("Error retrieving user corrections", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ControllerResponse<>(false, "Failed to retrieve corrections: " + e.getMessage()));
        }
    }

    // DTOs for request bodies
    public static class SubmitCorrectionRequestDTO {
        private UUID novelId;
        private UUID chapterId;
        private String s3Key;
        private Integer paragraphIndex;
        private String originalText;
        private String suggestedText;

        public SubmitCorrectionRequestDTO() {}

        public UUID getNovelId() { return novelId; }
        public void setNovelId(UUID novelId) { this.novelId = novelId; }

        public UUID getChapterId() { return chapterId; }
        public void setChapterId(UUID chapterId) { this.chapterId = chapterId; }

        public String getS3Key() { return s3Key; }
        public void setS3Key(String s3Key) { this.s3Key = s3Key; }

        public Integer getParagraphIndex() { return paragraphIndex; }
        public void setParagraphIndex(Integer paragraphIndex) { this.paragraphIndex = paragraphIndex; }

        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public String getSuggestedText() { return suggestedText; }
        public void setSuggestedText(String suggestedText) { this.suggestedText = suggestedText; }
    }

    public static class RejectCorrectionDTO {
        private String reason;

        public RejectCorrectionDTO() {}

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
