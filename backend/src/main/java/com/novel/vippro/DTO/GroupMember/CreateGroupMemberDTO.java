package com.novel.vippro.DTO.GroupMember;

import java.util.UUID;

import lombok.Data;

@Data
public class CreateGroupMemberDTO {
    private UUID userId;
    private UUID groupId;
    private Boolean isAdmin;
    private String displayName;
}