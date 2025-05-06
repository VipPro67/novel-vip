package com.novel.vippro.Controllers;

import java.util.List;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/role-approval")
public class RoleApprovalController {

        @Autowired
        private RoleApprovalService roleApprovalService;

        @Autowired
        private UserRepository userRepository;

        @PostMapping("/request")
        public ControllerResponse<?> requestRole(@RequestBody RoleRequestDTO request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                                .orElseThrow(() -> new RuntimeException("Error: User not found"));
                return ControllerResponse.success("Role request submitted successfully",
                                roleApprovalService.createRoleApprovalRequest(user, request.getRequestedRole()));
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

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/pending")
        public ControllerResponse<PageResponse<RoleApprovalDTO>> getAllPendingRequests(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                PageResponse<RoleApprovalDTO> requests = roleApprovalService.getAllPendingRequests(pageable);

                return ControllerResponse.success("Pending requests retrieved successfully", requests);
        }

        @GetMapping("/my-requests")
        public ControllerResponse<List<RoleApprovalDTO>> getMyRequests() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                                .orElseThrow(() -> new RuntimeException("Error: User not found"));

                List<RoleApprovalDTO> myRequests = roleApprovalService.getUserRequests(user);
                return ControllerResponse.success("My requests retrieved successfully", myRequests);
        }
}