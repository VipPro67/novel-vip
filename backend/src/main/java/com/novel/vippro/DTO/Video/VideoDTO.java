package com.novel.vippro.DTO.Video;

import com.novel.vippro.Models.Video.VideoPlatform;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoDTO {
    private UUID id;
    private String title;
    private String description;
    private String videoUrl;
    private String embedUrl;
    private VideoPlatform platform;
    private String externalId;
    private Instant createdAt;
    private Instant updatedAt;
}
