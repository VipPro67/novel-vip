package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.Models.ReadingHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReadingHistoryMapper {
	@Autowired
	private NovelMapper novelMapper;

	public ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory) {
		var dto = new ReadingHistoryDTO();
		dto.setUserId(readingHistory.getUser().getId());
		dto.setNovel(novelMapper.NoveltoDTO(readingHistory.getNovel()));
		dto.setLastReadChapterIndex(readingHistory.getLastReadChapterIndex());
		dto.setLastReadAt(readingHistory.getLastReadAt());
		return dto;
	}

	public List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories) {
		return readingHistories.stream()
				.map(this::ReadingHistorytoDTO)
				.collect(Collectors.toList());
	}
}
