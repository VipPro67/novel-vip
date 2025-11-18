package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.Models.FeatureRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeatureRequestMapper {

    @Mapping(target = "userId", source = "requester.id")
    @Mapping(target = "fullName", source = "requester.fullName")
    @Mapping(target = "username", source = "requester.username")
    FeatureRequestDTO RequesttoRequestDTO(FeatureRequest request);

    FeatureRequest RequestDTOtoRequest(FeatureRequestDTO requestDTO);

    FeatureRequest CreateFeatureRequestDTOtoFeatureRequest(CreateFeatureRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFeatureRequestFromDTO(FeatureRequestDTO dto, @MappingTarget FeatureRequest featureRequest);
}
