package com.novel.vippro.DTO.Auth;

import lombok.Builder;

@Builder
public record RefreshTokenRequest(
    String refreshToken
) {}