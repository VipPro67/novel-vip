package com.novel.vippro.DTO.User;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateDTO(
    @Size(max = 120)
    String fullName,
    String avatar
) {}