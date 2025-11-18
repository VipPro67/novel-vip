package com.novel.vippro.DTO.Genre;

import lombok.Builder;
import java.util.UUID;

@Builder
public record GenreDTO(
    UUID id,
    String name,
    String description
) {}
