package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Novel.NovelDetailDTO;
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

		return novelDTO;
	}

	public NovelDetailDTO NoveltoNovelDetailDTO(Novel novel) {
		return modelMapper.map(novel, NovelDetailDTO.class);
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