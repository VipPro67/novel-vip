package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.Models.FeatureRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureRequestMapper {
	@Autowired
	private ModelMapper modelMapper;

	public FeatureRequestDTO RequesttoRequestDTO(FeatureRequest request) {
		return modelMapper.map(request, FeatureRequestDTO.class);
	}

	public FeatureRequest RequestDTOtoRequest(FeatureRequestDTO requestDTO) {
		return modelMapper.map(requestDTO, FeatureRequest.class);
	}

	public FeatureRequest CreateFeatureRequestDTOtoFeatureRequest(CreateFeatureRequestDTO requestDTO) {
		return modelMapper.map(requestDTO, FeatureRequest.class);
	}

	public void updateFeatureRequestFromDTO(FeatureRequestDTO dto, FeatureRequest featureRequest) {
		modelMapper.map(dto, featureRequest);
	}
}
