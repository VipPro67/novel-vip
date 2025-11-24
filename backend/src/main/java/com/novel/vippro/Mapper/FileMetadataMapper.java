package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.Models.FileMetadata;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface FileMetadataMapper {

    FileMetadataDTO FileMetadataToDTO(FileMetadata metadata);

    FileMetadata DTOToFileMetadata(FileMetadataDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFileMetadataFromDTO(FileMetadataDTO dto, @MappingTarget FileMetadata fileMetadata);
}
