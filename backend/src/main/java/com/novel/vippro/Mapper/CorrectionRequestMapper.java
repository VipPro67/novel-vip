package com.novel.vippro.Mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.novel.vippro.Models.CorrectionRequest;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestWithDetailsDTO;

@Mapper(componentModel = "spring", uses = {UserMapper.class, NovelMapper.class, ChapterMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CorrectionRequestMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "novelId", source = "novel.id")
	@Mapping(target = "chapterId", source = "chapter.id")
	@Mapping(target = "paragraphIndices", source = "paragraphIndices", qualifiedByName = "stringToList")
	@Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLocalDateTime")
	@Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLocalDateTime")
	CorrectionRequestDTO toDto(CorrectionRequest entity);

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "novelId", source = "novel.id")
	@Mapping(target = "chapterId", source = "chapter.id")
	@Mapping(target = "paragraphIndices", source = "paragraphIndices", qualifiedByName = "stringToList")
	@Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLocalDateTime")
	@Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLocalDateTime")
	@Mapping(target = "user", source = "user")
	@Mapping(target = "novel", source = "novel")
	@Mapping(target = "chapter", source = "chapter")
	CorrectionRequestWithDetailsDTO toDetailsDto(CorrectionRequest entity);

	// For use with .map() in Page and Stream
	default CorrectionRequestDTO apply(CorrectionRequest entity) {
		return toDto(entity);
	}

	@Named("instantToLocalDateTime")
	default LocalDateTime instantToLocalDateTime(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	@Named("stringToList")
	default List<Integer> stringToList(String paragraphIndices) {
		if (paragraphIndices == null || paragraphIndices.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.stream(paragraphIndices.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(Integer::parseInt)
			.collect(Collectors.toList());
	}

	@Named("listToString")
	default String listToString(List<Integer> indices) {
		if (indices == null || indices.isEmpty()) {
			return null;
		}
		return indices.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}
}
