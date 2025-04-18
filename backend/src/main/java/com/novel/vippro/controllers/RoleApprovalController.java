package com.novel.vippro.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.models.User;
import com.novel.vippro.payload.request.RoleRequestRequest;
import com.novel.vippro.payload.request.RoleRejectRequest;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.payload.response.RoleApprovalResponse;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.security.services.UserDetailsImpl;
import com.novel.vippro.services.RoleApprovalService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/role-approval")
public class RoleApprovalController {

        @Autowired
        private RoleApprovalService roleApprovalService;

        @Autowired
        private UserRepository userRepository;

        @PostMapping("/request")
        public ControllerResponse<?> requestRole(@RequestBody RoleRequestRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                                .orElseThrow(() -> new RuntimeException("Error: User not found"));

                return ControllerResponse
                                .success(
                                                "Role request submitted successfully",
                                                roleApprovalService.createRoleApprovalRequest(user,
                                                                request.getRequestedRole()));
        }

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

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/reject/{requestId}")
        public ControllerResponse<?> rejectRequest(@PathVariable UUID requestId,
                        @RequestBody RoleRejectRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                return ControllerResponse
                                .success(
                                                "Role request rejected successfully",
                                                roleApprovalService.rejectRoleRequest(requestId,
                                                                userDetails.getUsername(),
                                                                request.getReason()));
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/pending")
        public ControllerResponse<List<RoleApprovalResponse>> getAllPendingRequests() {
                List<RoleApprovalResponse> pendingRequests = roleApprovalService.getAllPendingRequests().stream()
                                .map(RoleApprovalResponse::new)
                                .collect(Collectors.toList());
                return ControllerResponse.success("Pending requests retrieved successfully", pendingRequests);
        }

        @GetMapping("/my-requests")
        public ControllerResponse<List<RoleApprovalResponse>> getMyRequests() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                                .orElseThrow(() -> new RuntimeException("Error: User not found"));

                List<RoleApprovalResponse> myRequests = roleApprovalService.getUserRequests(user).stream()
                                .map(RoleApprovalResponse::new)
                                .collect(Collectors.toList());
                return ControllerResponse.success("My requests retrieved successfully", myRequests);
        }
}