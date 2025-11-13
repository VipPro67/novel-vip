package com.novel.vippro.DTO.Group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateGroupDTO(
    @NotBlank(message = "Group name cannot be blank")
    @Size(min = 3, max = 50, message = "Group name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Group name can only contain letters, numbers, spaces, and underscores")
    String name,

    @NotBlank(message = "Group description cannot be blank")
    @Size(min = 10, max = 200, message = "Group description must be between 10 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.,;:!?'\"()\\s]*$", message = "Group description can only contain letters, numbers, spaces, and punctuation")
    String description
) {}
