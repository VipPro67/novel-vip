package com.novel.vippro.DTO.Message;

import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.User.UserDTO;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record MessageDTO(
    UUID id,
    Boolean isActive,
    Boolean isDeleted,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    UserDTO sender,
    UserDTO receiver,
    GroupDTO group,
    String content,
    Boolean isRead
) {}