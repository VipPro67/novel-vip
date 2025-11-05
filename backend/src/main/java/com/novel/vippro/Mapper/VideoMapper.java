package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Video.CreateVideoDTO;
import com.novel.vippro.DTO.Video.VideoDTO;
import com.novel.vippro.Models.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {

    public VideoDTO VideoToDTO(Video video) {
        if (video == null) {
            return null;
        }

        return VideoDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .embedUrl(video.getEmbedUrl())
                .platform(video.getPlatform())
                .externalId(video.getExternalId())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    public Video CreateVideoDTOtoVideo(CreateVideoDTO dto) {
        if (dto == null) {
            return null;
        }

        Video video = new Video();
        video.setTitle(dto.getTitle() != null ? dto.getTitle().trim() : null);
        video.setDescription(dto.getDescription());
        video.setVideoUrl(dto.getVideoUrl() != null ? dto.getVideoUrl().trim() : null);
        return video;
    }

    public void updateVideoFromDTO(VideoDTO dto, Video video) {
        if (dto == null || video == null) {
            return;
        }

        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setVideoUrl(dto.getVideoUrl());
        video.setEmbedUrl(dto.getEmbedUrl());
        video.setPlatform(dto.getPlatform());
        video.setExternalId(dto.getExternalId());
    }
}
