package com.novel.vippro.DTO.Review;

import lombok.Getter;
import lombok.Setter;

import com.novel.vippro.DTO.base.BaseDTO;

import java.util.UUID;

@Getter
@Setter
public class ReviewDTO extends BaseDTO {
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
}