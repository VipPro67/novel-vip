package com.novel.vippro.DTO.FeatureRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.FeatureRequest;
import com.novel.vippro.Models.User;

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
    private boolean hasVoted;

    public FeatureRequestDTO(FeatureRequest featureRequest, User user) {
        this.title = featureRequest.getTitle();
        this.description = featureRequest.getDescription();
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.status = featureRequest.getStatus();
        this.voteCount = featureRequest.getVoteCount();
        this.hasVoted = featureRequest.getVoters().contains(user);
    }
}