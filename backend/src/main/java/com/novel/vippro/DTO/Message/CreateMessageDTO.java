package com.novel.vippro.DTO.Message;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageDTO {

    private UUID receiverId;
    private UUID groupId;
    @NotBlank(message = "Content cannot be blank")
    private String content;
}