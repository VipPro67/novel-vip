package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Report.ReportCreateDTO;
import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.DTO.Report.ReportUpdateDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.ReportService;

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
        public ControllerResponse<PageResponse<ReportDTO>> getAllReports(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
                PageResponse<ReportDTO> reports = reportService.getAllReports(pageable);
                return ControllerResponse.success("Reports retrieved successfully", reports);
        }

        @Operation(summary = "Get pending reports", description = "Get all pending reports (admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized")
        })
        @GetMapping("/pending")
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<PageResponse<ReportDTO>> getPendingReports(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<ReportDTO> reports = reportService.getPendingReports(pageable);
                return ControllerResponse.success("Pending reports retrieved successfully", reports);
        }

        @Operation(summary = "Get user reports", description = "Get all reports submitted by a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/user/{userId}")
        @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId)")
        public ControllerResponse<PageResponse<ReportDTO>> getUserReports(
                        @Parameter(description = "User ID", required = true) @PathVariable UUID userId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<ReportDTO> reports = reportService.getUserReports(userId, pageable);
                return ControllerResponse.success("User reports retrieved successfully", reports);
        }

        @Operation(summary = "Get novel reports", description = "Get all reports for a novel (admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Novel not found")
        })
        @GetMapping("/novel/{novelId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<PageResponse<ReportDTO>> getNovelReports(
                        @Parameter(description = "Novel ID", required = true) @PathVariable UUID novelId,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<ReportDTO> reports = reportService.getNovelReports(novelId, pageable);
                return ControllerResponse.success("Novel reports retrieved successfully", reports);
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
        public ControllerResponse<ReportDTO> createReport(
                        @Parameter(description = "Report details", required = true) @Valid @RequestBody ReportCreateDTO reportDTO) {
                ReportDTO report = reportService.createReport(reportDTO);
                return ControllerResponse.success("Report submitted successfully", report);
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
        public ControllerResponse<ReportDTO> updateReportStatus(
                        @Parameter(description = "Report ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Status update details", required = true) @Valid @RequestBody ReportUpdateDTO updateDTO) {
                ReportDTO report = reportService.updateReportStatus(id, updateDTO);
                return ControllerResponse.success("Report status updated successfully", report);
        }

        @GetMapping("/my-reports")
        @PreAuthorize("hasRole('ADMIN') or @reportService.isReportOwner(#id)")
        public ControllerResponse<PageResponse<ReportDTO>> getMyReport( @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
                PageResponse<ReportDTO> reports = reportService.getMyReport(pageable);

                return ControllerResponse.success("Novel reports retrieved successfully", reports);                
        }

        @Operation(summary = "Get report", description = "Get details of a specific report")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Report retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Report not found")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @reportService.isReportOwner(#id)")
        public ControllerResponse<ReportDTO> getReport(
                        @Parameter(description = "Report ID", required = true) @PathVariable UUID id) {
                ReportDTO report = reportService.getReport(id);
                return ControllerResponse.success("Report retrieved successfully", report);
        }


}