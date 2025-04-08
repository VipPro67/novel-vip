package com.novel.vippro.payload.response;

import java.time.LocalDateTime;

import com.novel.vippro.models.RoleApprovalRequest;

public class RoleApprovalResponse {
    private Long id;
    private Long userId;
    private String username;
    private String requestedRole;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private String processedBy;
    private String rejectionReason;

    public RoleApprovalResponse(RoleApprovalRequest request) {
        this.id = request.getId();
        this.userId = request.getUser().getId();
        this.username = request.getUser().getUsername();
        this.requestedRole = request.getRequestedRole().getName().name();
        this.status = request.getStatus();
        this.requestDate = request.getRequestDate();
        this.processedDate = request.getProcessedDate();
        this.processedBy = request.getProcessedBy();
        this.rejectionReason = request.getRejectionReason();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
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
}