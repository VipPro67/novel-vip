package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.NovelSource.NovelSourceDTO;
import com.novel.vippro.Models.NovelSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NovelSourceMapper {

    NovelSourceMapper INSTANCE = Mappers.getMapper(NovelSourceMapper.class);

    @Mapping(source = "novel.id", target = "novelId")
    @Mapping(source = "novel.title", target = "novelTitle")
    NovelSourceDTO toDTO(NovelSource entity);

    @Mapping(target = "novel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    NovelSource toEntity(NovelSourceDTO dto);
}
