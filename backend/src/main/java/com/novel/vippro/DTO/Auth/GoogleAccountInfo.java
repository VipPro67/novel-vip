package com.novel.vippro.DTO.Auth;

import lombok.Builder;

@Builder
public record GoogleAccountInfo(
    String email,
    boolean emailVerified,
    String fullName,
    String avatar,
    String subject
) {}
