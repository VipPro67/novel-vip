package com.novel.vippro.DTO.GroupMember;

import lombok.Builder;
import java.util.UUID;

@Builder
public record CreateGroupMemberDTO(
    UUID userId,
    UUID groupId,
    Boolean isAdmin,
    String displayName
) {}