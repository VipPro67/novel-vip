package com.novel.vippro.DTO.GroupMember;

import lombok.Builder;
import java.util.UUID;

@Builder
public record GroupMemberDTO(
    UUID id,
    UUID userId,
    UUID groupId,
    Boolean isAdmin,
    String displayName,
    String joinedAt
) {}