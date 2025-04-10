package com.novel.vippro.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.models.ERole;
import com.novel.vippro.models.Role;
import com.novel.vippro.models.RoleApprovalRequest;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.MessageResponse;
import com.novel.vippro.repository.RoleApprovalRequestRepository;
import com.novel.vippro.repository.RoleRepository;
import com.novel.vippro.repository.UserRepository;

@Service
public class RoleApprovalService {
    private static final Logger logger = LoggerFactory.getLogger(RoleApprovalService.class);

    @Autowired
    private RoleApprovalRequestRepository roleApprovalRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public ResponseEntity<?> createRoleApprovalRequest(User user, ERole requestedRole) {
        // Check if user already has a pending request for this role
        Optional<RoleApprovalRequest> existingRequest = roleApprovalRequestRepository
                .findByUserAndStatus(user, "PENDING");

        if (existingRequest.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You already have a pending role request"));
        }

        // Check if user already has the requested role
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == requestedRole);

        if (hasRole) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You already have the requested role"));
        }

        // Create new role approval request
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        RoleApprovalRequest request = new RoleApprovalRequest(user, role);
        roleApprovalRequestRepository.save(request);

        logger.info("Created role approval request for user {} requesting role {}",
                user.getUsername(), requestedRole);

        return ResponseEntity.ok(new MessageResponse("Role request submitted successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> approveRoleRequest(UUID requestId, String adminUsername) {
        RoleApprovalRequest request = roleApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Error: Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Request is not pending"));
        }

        // Update request status
        request.setStatus("APPROVED");
        request.setProcessedDate(LocalDateTime.now());
        request.setProcessedBy(adminUsername);
        roleApprovalRequestRepository.save(request);

        // Add role to user
        User user = request.getUser();
        user.getRoles().add(request.getRequestedRole());
        userRepository.save(user);

        logger.info("Approved role request {} for user {}", requestId, user.getUsername());

        return ResponseEntity.ok(new MessageResponse("Role request approved successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> rejectRoleRequest(UUID requestId, String adminUsername, String reason) {
        RoleApprovalRequest request = roleApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Error: Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Request is not pending"));
        }

        // Update request status
        request.setStatus("REJECTED");
        request.setProcessedDate(LocalDateTime.now());
        request.setProcessedBy(adminUsername);
        request.setRejectionReason(reason);
        roleApprovalRequestRepository.save(request);

        logger.info("Rejected role request {} for user {}", requestId, request.getUser().getUsername());

        return ResponseEntity.ok(new MessageResponse("Role request rejected successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleApprovalRequest> getAllPendingRequests() {
        return roleApprovalRequestRepository.findByStatus("PENDING");
    }

    public List<RoleApprovalRequest> getUserRequests(User user) {
        return roleApprovalRequestRepository.findByUser(user);
    }
}