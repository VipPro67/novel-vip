package com.novel.vippro.dto;

import lombok.Data;

@Data
public class FeatureDTO {
    private String key;
    private String name;
    private String description;
    private boolean enabled;
}