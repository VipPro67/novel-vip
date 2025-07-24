package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.Models.ReaderSettings;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReaderSettingsMapper {
	@Autowired
	private ModelMapper modelMapper;

	public ReaderSettingsDTO ReaderSettingsToReaderSettingsDTO(ReaderSettings readerSettings) {
		return modelMapper.map(readerSettings, ReaderSettingsDTO.class);
	}

	public void updateReaderSettingsFromDTO(ReaderSettingsUpdateDTO dto, ReaderSettings settings) {
		modelMapper.map(dto, settings);
	}
}
