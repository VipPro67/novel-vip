package com.novel.vippro.DTO.Role;

import com.novel.vippro.Models.ERole;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RoleRequestDTO(
    @NotNull
    ERole requestedRole,
    String reason
) {}