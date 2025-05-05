package com.novel.vippro.DTO.Role;

import com.novel.vippro.Models.ERole;

import jakarta.validation.constraints.NotNull;

public class RoleRequestDTO {
    @NotNull
    private ERole requestedRole;

    public ERole getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(ERole requestedRole) {
        this.requestedRole = requestedRole;
    }
}