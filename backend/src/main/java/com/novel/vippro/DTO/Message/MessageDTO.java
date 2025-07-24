package com.novel.vippro.DTO.Message;

import java.util.UUID;

import lombok.Data;

@Data
public class MessageDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private UUID groupId;
    private String content;
    private Boolean isRead;
    private String createdAt;
    private String updatedAt;
}