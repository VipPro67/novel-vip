package com.novel.vippro.payload.request;

import com.novel.vippro.models.ERole;
import jakarta.validation.constraints.NotNull;

public class RoleRequestRequest {
    @NotNull
    private ERole requestedRole;

    public ERole getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(ERole requestedRole) {
        this.requestedRole = requestedRole;
    }
}