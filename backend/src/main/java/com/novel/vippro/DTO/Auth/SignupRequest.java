package com.novel.vippro.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record SignupRequest(
    @NotBlank
    @Size(max = 50)
    @Email
    String email,

    @NotBlank
    String username,

    Set<String> role,

    @NotBlank
    String password
) {}