package com.novel.vippro.DTO.Auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleAccountInfo {
    private final String email;
    private final boolean emailVerified;
    private final String fullName;
    private final String avatar;
    private final String subject;
}
