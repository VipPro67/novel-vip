package com.novel.vippro.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewDTO {
    private UUID id;
    private UUID novelId;
    private String novelTitle;
    private UUID userId;
    private String username;
    private String userAvatar;
    private String title;
    private String content;
    private int rating;
    private boolean isVerifiedPurchase;
    private int helpfulVotes;
    private int unhelpfulVotes;
    private boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}