package com.novel.vippro.DTO.Group;

import java.util.UUID;

import lombok.Data;

@Data
public class GroupDTO {
    private UUID id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
}