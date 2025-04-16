package com.novel.vippro.controllers;

import com.novel.vippro.dto.ReportDTO;
import com.novel.vippro.dto.ReportCreateDTO;
import com.novel.vippro.dto.ReportUpdateDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Content report management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "Get all reports", description = "Get all reports (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Page<ReportDTO>>> getAllReports(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportDTO> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(ControllerResponse.success("Reports retrieved successfully", reports));
    }

    @Operation(summary = "Get pending reports", description = "Get all pending reports (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Page<ReportDTO>>> getPendingReports(
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportDTO> reports = reportService.getPendingReports(pageable);
        return ResponseEntity.ok(ControllerResponse.success("Pending reports retrieved successfully", reports));
    }

    @Operation(summary = "Get user reports", description = "Get all reports submitted by a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<ControllerResponse<Page<ReportDTO>>> getUserReports(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportDTO> reports = reportService.getUserReports(userId, pageable);
        return ResponseEntity.ok(ControllerResponse.success("User reports retrieved successfully", reports));
    }

    @Operation(summary = "Get novel reports", description = "Get all reports for a novel (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Novel not found")
    })
    @GetMapping("/novel/{novelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<Page<ReportDTO>>> getNovelReports(
            @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportDTO> reports = reportService.getNovelReports(novelId, pageable);
        return ResponseEntity.ok(ControllerResponse.success("Novel reports retrieved successfully", reports));
    }

    @Operation(summary = "Submit report", description = "Submit a new content report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid report data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Reported content not found")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ControllerResponse<ReportDTO>> createReport(
            @Parameter(description = "Report details", required = true) @Valid @RequestBody ReportCreateDTO reportDTO) {
        ReportDTO report = reportService.createReport(reportDTO);
        return ResponseEntity.ok(ControllerResponse.success("Report submitted successfully", report));
    }

    @Operation(summary = "Update report status", description = "Update the status of a report (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status update"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ControllerResponse<ReportDTO>> updateReportStatus(
            @Parameter(description = "Report ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Status update details", required = true) @Valid @RequestBody ReportUpdateDTO updateDTO) {
        ReportDTO report = reportService.updateReportStatus(id, updateDTO);
        return ResponseEntity.ok(ControllerResponse.success("Report status updated successfully", report));
    }

    @Operation(summary = "Get report", description = "Get details of a specific report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reportService.isReportOwner(#id)")
    public ResponseEntity<ControllerResponse<ReportDTO>> getReport(
            @Parameter(description = "Report ID", required = true) @PathVariable UUID id) {
        ReportDTO report = reportService.getReport(id);
        return ResponseEntity.ok(ControllerResponse.success("Report retrieved successfully", report));
    }
}