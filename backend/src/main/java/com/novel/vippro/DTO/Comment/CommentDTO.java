package com.novel.vippro.DTO.Comment;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

@Data
public class CommentDTO {
    private UUID id;
    private String content;
    private UUID userId;
    private String username;
    private UUID novelId;
    private UUID chapterId;
    private UUID parentId;
    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdAt;
    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant updatedAt;
}
