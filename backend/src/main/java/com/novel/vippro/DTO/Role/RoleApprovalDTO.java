package com.novel.vippro.DTO.Role;

import java.time.Instant;
import java.util.UUID;

import com.novel.vippro.Models.ERole;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RoleApprovalDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private ERole requestedRole;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String processedBy;
    private String rejectionReason;
}
