package com.novel.vippro.DTO.Role;

import com.novel.vippro.Models.ERole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequestDTO {
    @NotNull
    private ERole requestedRole;

    private String reason;

    public ERole getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(ERole requestedRole) {
        this.requestedRole = requestedRole;
    }
}