package com.novel.vippro.Services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.DTO.Role.RoleRequestDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.ERole;
import com.novel.vippro.Models.Role;
import com.novel.vippro.Models.RoleApprovalRequest;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.RoleApprovalRequestRepository;
import com.novel.vippro.Repository.RoleRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

@Service
public class RoleApprovalService {
    private static final Logger logger = LoggerFactory.getLogger(RoleApprovalService.class);

    @Autowired
    private RoleApprovalRequestRepository roleApprovalRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Mapper mapper;

    @Transactional
    public RoleApprovalDTO createRoleApprovalRequest(RoleRequestDTO request) {
        // Check if user already has a pending request for this role
        User user = userRepository.findById(UserDetailsImpl.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        Role requestedRoleEntity = roleRepository.findByName(request.getRequestedRole())
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
        ERole requestedRole = request.getRequestedRole();   
        Optional<RoleApprovalRequest> existingRequest = roleApprovalRequestRepository
                .findByUserAndStatus(user, "PENDING", requestedRoleEntity);

        if (existingRequest.isPresent()) {
            throw new RuntimeException("Error: You already have a pending request for this role");
        }

        // Check if user already has the requested role
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == requestedRole);

        if (hasRole) {
            throw new RuntimeException("Error: You already have this role");
        }

        // Create new role approval request
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        RoleApprovalRequest roleRequest = new RoleApprovalRequest(user, role, request.getReason());
        roleApprovalRequestRepository.save(roleRequest);

        logger.info("Created role approval request for user {} requesting role {}",
                user.getUsername(), requestedRole);
        return mapper.RoleApprovalRequestToDTO(roleRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RoleApprovalDTO approveRoleRequest(UUID requestId, String adminUsername) {
        RoleApprovalRequest request = roleApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Error: Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Error: Request is not pending");
        }

        // Update request status
        request.setStatus("APPROVED");
        request.setUpdatedAt(Instant.now());
        request.setProcessedBy(adminUsername);
        roleApprovalRequestRepository.save(request);

        // Add role to user
        User user = request.getUser();
        user.getRoles().add(request.getRequestedRole());
        userRepository.save(user);

        logger.info("Approved role request {} for user {}", requestId, user.getUsername());

        return mapper.RoleApprovalRequestToDTO(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RoleApprovalDTO rejectRoleRequest(UUID requestId, String adminUsername, String reason) {
        RoleApprovalRequest request = roleApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Error: Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Error: Request is not pending");
        }

        // Update request status
        request.setStatus("REJECTED");
        request.setUpdatedAt(Instant.now());
         request.setProcessedBy(adminUsername);
        request.setRejectionReason(reason);
        roleApprovalRequestRepository.save(request);

        logger.info("Rejected role request {} for user {}", requestId, request.getUser().getUsername());

        return mapper.RoleApprovalRequestToDTO(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PageResponse<RoleApprovalDTO> getAllPendingRequests(Pageable pageable) {
        Page<RoleApprovalRequest> requests = roleApprovalRequestRepository
                .findByStatus("PENDING", pageable);
        return new PageResponse<>(requests.map(mapper::RoleApprovalRequestToDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleApprovalDTO> getUserRequests(Pageable pageable) {
        var userId = UserDetailsImpl.getCurrentUserId();
        Page<RoleApprovalRequest> requests = roleApprovalRequestRepository
                .findByUserId(userId, pageable);
        return new PageResponse<>(requests.map(mapper::RoleApprovalRequestToDTO));
    }
}