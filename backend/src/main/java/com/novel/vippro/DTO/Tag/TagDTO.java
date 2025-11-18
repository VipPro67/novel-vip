package com.novel.vippro.DTO.Tag;

import lombok.Builder;
import java.util.UUID;

@Builder
public record TagDTO(
    UUID id,
    String name,
    String description
) {}
