package com.novel.vippro.payload.request;

import jakarta.validation.constraints.NotBlank;

public class RoleRejectRequest {
    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}