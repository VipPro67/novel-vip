package com.novel.vippro.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
}
