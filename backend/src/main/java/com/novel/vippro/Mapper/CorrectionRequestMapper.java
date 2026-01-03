package com.novel.vippro.Mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
// import com.novel.vippro.Models.CorrectionRequest;
// import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.novel.vippro.Models.CorrectionRequest;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CorrectionRequestMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "novelId", source = "novel.id")
	@Mapping(target = "chapterId", source = "chapter.id")
	@Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLocalDateTime")
	@Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLocalDateTime")
	CorrectionRequestDTO toDto(CorrectionRequest entity);

	// For use with .map() in Page and Stream
	default CorrectionRequestDTO apply(CorrectionRequest entity) {
		return toDto(entity);
	}

	@Named("instantToLocalDateTime")
	default LocalDateTime instantToLocalDateTime(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
}
