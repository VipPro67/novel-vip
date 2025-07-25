package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.Models.ReadingHistory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReadingHistoryMapper {
	@Autowired
	private ModelMapper modelMapper;

	public ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory) {
		return modelMapper.map(readingHistory, ReadingHistoryDTO.class);
	}

	public List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories) {
		return readingHistories.stream()
				.map(this::ReadingHistorytoDTO)
				.collect(Collectors.toList());
	}
}
