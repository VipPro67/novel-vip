package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.NovelDocument;
import com.novel.vippro.Models.Tag;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NovelMapper {

	@Autowired
	private ModelMapper modelMapper;

	public NovelDTO NoveltoDTO(Novel novel) {
		NovelDTO novelDTO = modelMapper.map(novel, NovelDTO.class);
		if(novel.getCategories() != null) {
		novelDTO.setCategories(novel.getCategories().stream()
		.map(category -> new CategoryDTO(category.getId(), category.getName(),
		category.getDescription()))
		.collect(Collectors.toSet()));
		}
		if(novel.getTags() != null) {
		novelDTO.setTags(novel.getTags().stream()
		.map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.getDescription()))
		.collect(Collectors.toSet()));
		}
		if(novel.getGenres() != null) {
		novelDTO.setGenres(novel.getGenres().stream()
		.map(genre -> new GenreDTO(genre.getId(), genre.getName(),
		genre.getDescription()))
		.collect(Collectors.toSet()));
		}
		return novelDTO;
	}

    public static NovelDocument toDocument(Novel novel) {
        NovelDocument doc = new NovelDocument();
        doc.setId(novel.getId());
        doc.setTitle(novel.getTitle());
        doc.setDescription(novel.getDescription());
        doc.setAuthor(novel.getAuthor());
        doc.setStatus(novel.getStatus());
        doc.setCategories(novel.getCategories()
                .stream().map(Category::getName).collect(Collectors.toList()));
        doc.setTags(novel.getTags()
                .stream().map(Tag::getName).collect(Collectors.toList()));
        doc.setGenres(novel.getGenres()
                .stream().map(Genre::getName).collect(Collectors.toList()));
        doc.setPublic(novel.isPublic());
        doc.setTotalChapters(novel.getTotalChapters());
        doc.setViews(novel.getViews());
        doc.setRating(novel.getRating());
        doc.setCreatedAt(novel.getCreatedAt().atZone(ZoneOffset.UTC).toInstant());
        doc.setUpdatedAt(novel.getUpdatedAt().atZone(ZoneOffset.UTC).toInstant());
        return doc;
    }

	public List<NovelDTO> NovelListtoDTOList(List<Novel> novels) {
		return novels.stream()
				.map(this::NoveltoDTO)
				.collect(Collectors.toList());
	}

	public void updateNovelFromDTO(NovelDTO dto, Novel novel) {
		modelMapper.map(dto, novel);
	}
}