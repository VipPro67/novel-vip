package com.novel.vippro.DTO.Message;

import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

import lombok.Data;

@Data
public class MessageDTO extends BaseDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private UUID groupId;
    private String content;
    private Boolean isRead;
}