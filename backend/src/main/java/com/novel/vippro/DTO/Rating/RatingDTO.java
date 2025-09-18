package com.novel.vippro.DTO.Rating;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

@Data
public class RatingDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID novelId;
    private String novelTitle;
    private Integer score;
    private String review;
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdAt;
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant updatedAt;
}