package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Models.Novel;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	public List<NovelDTO> NovelListtoDTOList(List<Novel> novels) {
		return novels.stream()
				.map(this::NoveltoDTO)
				.collect(Collectors.toList());
	}

	public void updateNovelFromDTO(NovelDTO dto, Novel novel) {
		modelMapper.map(dto, novel);
	}
}