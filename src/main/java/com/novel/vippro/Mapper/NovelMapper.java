package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;
import com.novel.vippro.Models.Tag;
import com.novel.vippro.Services.FileStorageService;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { CategoryMapper.class, TagMapper.class, GenreMapper.class }, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class NovelMapper {

    @Autowired
    @Qualifier("s3FileStorageService")
    protected FileStorageService fileStorageService;

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "totalViews", expression = "java(novel.getTotalViews() == null ? null : novel.getTotalViews().intValue())")
    @Mapping(target = "monthlyViews", expression = "java(novel.getMonthlyViews() == null ? null : novel.getMonthlyViews().intValue())")
    @Mapping(target = "dailyViews", expression = "java(novel.getDailyViews() == null ? null : novel.getDailyViews().intValue())")
    public abstract NovelDTO NoveltoDTO(Novel novel);

    public abstract List<NovelDTO> NovelListtoDTOList(List<Novel> novels);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateNovelFromDTO(NovelDTO dto, @MappingTarget Novel novel);

    public NovelDocument toDocument(Novel novel) {
        NovelDocument doc = new NovelDocument();
        doc.setId(novel.getId());
        doc.setTitle(novel.getTitle());
        doc.setSlug(novel.getSlug());
        doc.setDescription(novel.getDescription());
        doc.setAuthor(novel.getAuthor());
        doc.setStatus(novel.getStatus());
        if (novel.getCategories() != null) {
            doc.setCategories(novel.getCategories().stream().map(Category::getName).collect(Collectors.toList()));
        }
        if (novel.getTags() != null) {
            doc.setTags(novel.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }
        if (novel.getGenres() != null) {
            doc.setGenres(novel.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
        }
        doc.setPublic(novel.isPublic());
        doc.setTotalChapters(novel.getTotalChapters());
        doc.setRating(novel.getRating());
        if (novel.getCoverImage() != null) {
            FileMetadataDTO.builder()
                    .id(novel.getCoverImage().getId())
                    .fileName(novel.getCoverImage().getFileName())
                    .contentType(novel.getCoverImage().getContentType())
                    .size(novel.getCoverImage().getSize())
                    .type(novel.getCoverImage().getType())
                    .publicId(novel.getCoverImage().getPublicId())
                    .fileUrl(novel.getCoverImage().getFileUrl())
                    .build();
        }
        if (novel.getCreatedAt() != null) {
            doc.setCreatedAt(novel.getCreatedAt().atZone(ZoneOffset.UTC).toInstant());
        }
        if (novel.getUpdatedAt() != null) {
            doc.setUpdatedAt(novel.getUpdatedAt().atZone(ZoneOffset.UTC).toInstant());
        }
        return doc;
    }

    public static Novel DocumenttoNovel(NovelDocument doc) {
        Novel n = new Novel();
        n.setId(doc.getId());
        n.setTitle(doc.getTitle());
        n.setSlug(doc.getSlug());
        n.setDescription(doc.getDescription());
        n.setAuthor(doc.getAuthor());
        n.setStatus(doc.getStatus());
        n.setPublic(doc.isPublic());
        n.setTotalChapters(doc.getTotalChapters());
        n.setRating(doc.getRating());

        if (doc.getCreatedAt() != null) {
            n.setCreatedAt(doc.getCreatedAt());
        }
        if (doc.getUpdatedAt() != null) {
            n.setUpdatedAt(doc.getUpdatedAt());
        }

        if (doc.getCategories() != null) {
            n.setCategories(doc.getCategories().stream().filter(Objects::nonNull).map(Category::new)
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        if (doc.getTags() != null) {
            n.setTags(doc.getTags().stream().filter(Objects::nonNull).map(Tag::new)
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        if (doc.getGenres() != null) {
            n.setGenres(doc.getGenres().stream().filter(Objects::nonNull).map(Genre::new)
                    .collect(Collectors.toCollection(HashSet::new)));
        }

        return n;
    }

    @AfterMapping
    protected void populateImageUrl(Novel novel, @MappingTarget NovelDTO.NovelDTOBuilder builder) {
        if (novel != null && novel.getCoverImage() != null) {
            builder.imageUrl(fileStorageService.generateFileUrl(novel.getCoverImage().getPublicId(), Integer.MAX_VALUE));
        }
    }
}