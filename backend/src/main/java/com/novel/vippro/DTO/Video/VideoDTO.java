package com.novel.vippro.DTO.Video;

import com.novel.vippro.Models.Video.VideoPlatform;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

public record VideoDTO(
    UUID id,
    String title,
    String description,
    String videoUrl,
    String embedUrl,
    VideoPlatform platform,
    String externalId,
    Instant createdAt,
    Instant updatedAt
) {}
