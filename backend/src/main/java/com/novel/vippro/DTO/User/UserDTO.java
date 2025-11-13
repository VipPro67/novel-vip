package com.novel.vippro.DTO.User;

import com.novel.vippro.Models.Role;
import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record UserDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String username,
    String email,
    String fullName,
    List<Role> roles
) {}