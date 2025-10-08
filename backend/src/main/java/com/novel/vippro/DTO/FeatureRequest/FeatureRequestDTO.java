package com.novel.vippro.DTO.FeatureRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.FeatureRequest;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequestDTO extends BaseDTO {
    private String title;
    private String description;
    private UUID userId;
    private String username;
    private String fullName;
    private FeatureRequest.FeatureRequestStatus status;
    private Integer voteCount;
}