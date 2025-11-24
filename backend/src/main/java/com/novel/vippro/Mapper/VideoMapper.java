package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Video.CreateVideoDTO;
import com.novel.vippro.DTO.Video.VideoDTO;
import com.novel.vippro.Models.Video;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface VideoMapper {

    VideoDTO VideoToDTO(Video video);
    
    Video CreateVideoDTOtoVideo(CreateVideoDTO createVideoDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVideoFromDTO(VideoDTO dto, @MappingTarget Video video);

} 
