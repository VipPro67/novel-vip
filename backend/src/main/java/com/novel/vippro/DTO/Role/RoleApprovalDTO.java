package com.novel.vippro.DTO.Role;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class RoleApprovalDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String requestedRole;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedBy;
    private String rejectionReason;
}
