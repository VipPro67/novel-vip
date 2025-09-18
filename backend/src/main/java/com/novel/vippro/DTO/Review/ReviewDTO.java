package com.novel.vippro.DTO.Review;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
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
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdAt;
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant updatedAt;
}