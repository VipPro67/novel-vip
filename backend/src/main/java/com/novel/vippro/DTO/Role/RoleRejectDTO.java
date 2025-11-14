package com.novel.vippro.DTO.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RoleRejectDTO(
    @NotBlank
    String reason
) {}