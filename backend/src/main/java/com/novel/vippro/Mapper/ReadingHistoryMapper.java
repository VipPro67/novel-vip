package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.Models.ReadingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = NovelMapper.class)
public interface ReadingHistoryMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "novel", source = "novel")
    ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory);

    List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories);
}
