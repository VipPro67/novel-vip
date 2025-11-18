package com.novel.vippro.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GoogleAuthRequest(
    @NotBlank
    String credential
) {}
