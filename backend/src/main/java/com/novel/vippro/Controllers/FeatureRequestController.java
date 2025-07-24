package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.Models.FeatureRequest;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.FeatureRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/feature-requests")
@Tag(name = "Feature Requests", description = "APIs for managing feature requests and voting")
@SecurityRequirement(name = "bearerAuth")
public class FeatureRequestController {

        @Autowired
        private FeatureRequestService featureRequestService;

        @Operation(summary = "Create feature request", description = "Create a new feature request that users can vote on")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature request created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PostMapping
        public ControllerResponse<FeatureRequestDTO> createFeatureRequest(
                        @Parameter(description = "Feature request details", required = true) @Valid @RequestBody CreateFeatureRequestDTO requestDTO,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                FeatureRequestDTO createdRequest = featureRequestService.createFeatureRequest(requestDTO, userId);
                return ControllerResponse.success("Feature request created successfully", createdRequest);
        }

        @Operation(summary = "Get feature request", description = "Retrieve a specific feature request by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature request retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Feature request not found"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/{id}")
        public ControllerResponse<FeatureRequestDTO> getFeatureRequest(
                        @Parameter(description = "Feature request ID", required = true) @PathVariable UUID id,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                FeatureRequestDTO request = featureRequestService.getFeatureRequest(id, userId);
                return ControllerResponse.success("Feature request retrieved successfully", request);
        }

        @Operation(summary = "Get all feature requests", description = "Retrieve all feature requests with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature requests retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping
        public ControllerResponse<PageResponse<FeatureRequestDTO>> getAllFeatureRequests(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
                PageResponse<FeatureRequestDTO> requests = featureRequestService.getAllFeatureRequests(pageable);
                return ControllerResponse.success("Feature requests retrieved successfully", requests);
        }

        @Operation(summary = "Get feature requests by status", description = "Retrieve feature requests filtered by status with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature requests retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid status"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/status/{status}")
        public ControllerResponse<PageResponse<FeatureRequestDTO>> getFeatureRequestsByStatus(
                        @Parameter(description = "Status to filter by (VOTING, PROCESSING, DONE, REJECTED)", required = true) @PathVariable FeatureRequest.FeatureRequestStatus status,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<FeatureRequestDTO> requests = featureRequestService.getFeatureRequestsByStatus(status,
                                pageable);
                return ControllerResponse.success("Feature requests retrieved successfully", requests);
        }

        @Operation(summary = "Vote for feature request", description = "Cast a vote for a specific feature request")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Vote recorded successfully"),
                        @ApiResponse(responseCode = "400", description = "Cannot vote for own request or already voted"),
                        @ApiResponse(responseCode = "404", description = "Feature request not found"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PostMapping("/{id}/vote")
        public ControllerResponse<FeatureRequestDTO> voteForFeatureRequest(
                        @Parameter(description = "Feature request ID", required = true) @PathVariable UUID id,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                FeatureRequestDTO updatedRequest = featureRequestService.voteForFeatureRequest(id, userId);
                return ControllerResponse.success("Vote recorded successfully", updatedRequest);
        }

        @Operation(summary = "Update feature request status", description = "Update the status of a feature request (admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
                        @ApiResponse(responseCode = "404", description = "Feature request not found"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PutMapping("/{id}/status")
        public ControllerResponse<FeatureRequestDTO> updateFeatureRequestStatus(
                        @Parameter(description = "Feature request ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "New status (VOTING, PROCESSING, DONE, REJECTED)", required = true) @RequestParam FeatureRequest.FeatureRequestStatus newStatus,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                FeatureRequestDTO updatedRequest = featureRequestService.updateFeatureRequestStatus(id, newStatus,
                                userId);
                return ControllerResponse.success("Status updated successfully", updatedRequest);
        }

        @Operation(summary = "Delete feature request", description = "Delete a feature request (creator or admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feature request deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized (creator or admin only)"),
                        @ApiResponse(responseCode = "404", description = "Feature request not found"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @DeleteMapping("/{id}")
        public ControllerResponse<Void> deleteFeatureRequest(
                        @Parameter(description = "Feature request ID", required = true) @PathVariable UUID id,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                featureRequestService.deleteFeatureRequest(id, userId);
                return ControllerResponse.success("Feature request deleted successfully", null);
        }
}