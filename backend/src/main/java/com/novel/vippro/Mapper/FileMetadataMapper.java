package com.novel.vippro.Mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.Models.FileMetadata;

@Component
public class FileMetadataMapper {
	@Autowired
	private ModelMapper modelMapper;

	public FileMetadataDTO FileMetadataToDTO(FileMetadata metadata) {
		if (metadata == null) {
			return null;
		}
		return modelMapper.map(metadata, FileMetadataDTO.class);
	}

	public FileMetadata DTOToFileMetadata(FileMetadataDTO dto) {
		if (dto == null) {
			return null;
		}
		return modelMapper.map(dto, FileMetadata.class);
	}

	public void updateFileMetadataFromDTO(FileMetadataDTO dto, FileMetadata fileMetadata) {
		modelMapper.map(dto, fileMetadata);
	}
}