package com.novel.vippro.DTO.Category;

import lombok.Builder;
import java.util.UUID;

@Builder
public record CategoryDTO(
    UUID id,
    String name,
    String description
) {}
