package com.novel.vippro.DTO.Group;

import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

import lombok.Data;

@Data
public class GroupDTO extends BaseDTO {
    private UUID id;
    private String name;
    private String description;
}