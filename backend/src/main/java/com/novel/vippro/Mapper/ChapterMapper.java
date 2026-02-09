package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Services.FileStorageService;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Mapper(componentModel = "spring", uses = { NovelMapper.class, FileMetadataMapper.class }, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class ChapterMapper {
    @Autowired
    @Qualifier("s3FileStorageService")
    protected FileStorageService fileStorageService;

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateChapterFromDTO(ChapterDTO dto, @MappingTarget Chapter chapter);

    @Mapping(target = "novelId", source = "novel.id")
    @Mapping(target = "novelTitle", source = "novel.title")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "isUnlocked", constant = "false")
    public abstract ChapterDetailDTO ChaptertoChapterDetailDTO(Chapter chapter);

    @AfterMapping
    protected void enrichDetailDto(Chapter chapter, @MappingTarget ChapterDetailDTO.ChapterDetailDTOBuilder builder) {
        if (chapter == null) {
            return;
        }
        if (chapter.getAudioFile() != null) {
            builder.audioUrl(fileStorageService.generateFileUrl(chapter.getAudioFile().getPublicId(), 21600));
        }
        if (chapter.getJsonFile() != null) {
            builder.jsonUrl(fileStorageService.generateFileUrl(chapter.getJsonFile().getPublicId(), 21600));
        }
    }
}
