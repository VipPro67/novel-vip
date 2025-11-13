package com.novel.vippro.DTO.Role;

import com.novel.vippro.Models.ERole;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record RoleApprovalDTO(
    UUID id,
    UUID userId,
    String username,
    ERole requestedRole,
    String status,
    Instant createdAt,
    Instant updatedAt,
    String processedBy,
    String rejectionReason
) {}
