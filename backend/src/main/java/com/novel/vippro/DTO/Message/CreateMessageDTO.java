package com.novel.vippro.DTO.Message;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.util.UUID;

@Builder
public record CreateMessageDTO(
    UUID receiverId,
    UUID groupId,
    @NotBlank(message = "Content cannot be blank")
    String content
) {}