package com.novel.vippro.dto;

import com.novel.vippro.models.FeatureRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequestDTO {
    private Long id;
    private String title;
    private String description;
    private UUID userId;
    private String username;
    private FeatureRequest.FeatureRequestStatus status;
    private Integer voteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasVoted;

    public static FeatureRequestDTO fromEntity(FeatureRequest featureRequest, boolean hasVoted) {
        FeatureRequestDTO dto = new FeatureRequestDTO();
        dto.setId(featureRequest.getId());
        dto.setTitle(featureRequest.getTitle());
        dto.setDescription(featureRequest.getDescription());
        dto.setUserId(featureRequest.getCreatedBy().getId());
        dto.setUsername(featureRequest.getCreatedBy().getUsername());
        dto.setStatus(featureRequest.getStatus());
        dto.setVoteCount(featureRequest.getVoteCount());
        dto.setCreatedAt(featureRequest.getCreatedAt());
        dto.setUpdatedAt(featureRequest.getUpdatedAt());
        dto.setHasVoted(hasVoted);
        return dto;
    }
}