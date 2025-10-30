package com.novel.vippro.Controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.DTO.Role.RoleRejectDTO;
import com.novel.vippro.DTO.Role.RoleRequestDTO;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.RoleApprovalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Tag(name = "Role Approval", description = "APIs for role approval management")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/role-approval")
public class RoleApprovalController {

    @Autowired
    private RoleApprovalService roleApprovalService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Request a new role", description = "Submit a request to gain a new role")
    @PostMapping("/request")
    public ControllerResponse<?> requestRole(@RequestBody RoleRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        return ControllerResponse.success("Role request submitted successfully",
                roleApprovalService.createRoleApprovalRequest(user, request));
    }

    @Operation(summary = "Approve a role request", description = "Approve a pending role request")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{requestId}")
    public ControllerResponse<?> approveRequest(@PathVariable UUID requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ControllerResponse
                .success(
                        "Role request approved successfully",
                        roleApprovalService.approveRoleRequest(requestId,
                                userDetails.getUsername()));
    }

    @Operation(summary = "Reject a role request", description = "Reject a pending role request with a reason")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{requestId}")
    public ControllerResponse<?> rejectRequest(@PathVariable UUID requestId,
            @RequestBody RoleRejectDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ControllerResponse
                .success(
                        "Role request rejected successfully",
                        roleApprovalService.rejectRoleRequest(requestId,
                                userDetails.getUsername(),
                                request.getReason()));
    }

    @Operation(summary = "Get all pending role requests", description = "Retrieve all pending role requests for admin review")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ControllerResponse<PageResponse<RoleApprovalDTO>> getAllPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<RoleApprovalDTO> requests = roleApprovalService.getAllPendingRequests(pageable);

        return ControllerResponse.success("Pending requests retrieved successfully", requests);
    }

    @Operation(summary = "Get my role requests", description = "Retrieve all role requests made by the authenticated user")
    @GetMapping("/my-requests")
    public ControllerResponse<PageResponse<RoleApprovalDTO>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir){
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var myRequests = roleApprovalService.getUserRequests(pageable);
		return ControllerResponse.success("User requests retrieved successfully", myRequests);

    }
}