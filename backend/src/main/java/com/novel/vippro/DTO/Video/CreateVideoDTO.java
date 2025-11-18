package com.novel.vippro.DTO.Video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record CreateVideoDTO(
    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 4000)
    String description,

    @NotBlank
    @Size(max = 1024)
    String videoUrl
) {}
