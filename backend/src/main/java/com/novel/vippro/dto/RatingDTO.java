package com.novel.vippro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RatingDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID novelId;
    private String novelTitle;
    private Integer score;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}