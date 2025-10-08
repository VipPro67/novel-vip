package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "role_approval_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "role_id" })
})
public class RoleApprovalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role requestedRole;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String processedBy;

    private String rejectionReason;
    public RoleApprovalRequest(User user, Role requestedRole) {
        this.user = user;
        this.requestedRole = requestedRole;
    }
}