package com.novel.vippro.DTO.Rating;

import lombok.Data;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;

@Data
public class RatingDTO extends BaseDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID novelId;
    private String novelTitle;
    private Integer score;
    private String review;
}