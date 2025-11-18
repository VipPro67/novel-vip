package com.novel.vippro.DTO.User;

import lombok.Builder;

@Builder
public record UserSearchDTO(
    String username,
    String email,
    String role,
    Boolean active,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}