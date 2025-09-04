package com.novel.vippro.Models;

import jakarta.persistence.*;
<<<<<<< Updated upstream
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
=======
import lombok.Data;
import com.novel.vippro.Models.base.BaseEntity;
>>>>>>> Stashed changes

@Entity
@Data
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

<<<<<<< Updated upstream
    @CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

=======
>>>>>>> Stashed changes
    private String processedBy;

    private String rejectionReason;

    public RoleApprovalRequest() {
    }

    public RoleApprovalRequest(User user, Role requestedRole) {
        this.user = user;
        this.requestedRole = requestedRole;
    }

<<<<<<< Updated upstream
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(Role requestedRole) {
        this.requestedRole = requestedRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRequestDate() {
        return createdAt;
    }

    public void setRequestDate(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessedDate() {
        return updatedAt;
    }

    public void setProcessedDate(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
=======
>>>>>>> Stashed changes
}