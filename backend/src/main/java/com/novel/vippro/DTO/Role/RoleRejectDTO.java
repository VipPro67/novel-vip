package com.novel.vippro.DTO.Role;

import jakarta.validation.constraints.NotBlank;

public class RoleRejectDTO {
    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}