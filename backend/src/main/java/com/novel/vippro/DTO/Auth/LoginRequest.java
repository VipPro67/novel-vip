package com.novel.vippro.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
    @NotBlank
    String email,

    @NotBlank
    String password
) {}