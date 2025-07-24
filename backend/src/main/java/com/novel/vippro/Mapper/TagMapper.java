package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Models.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TagMapper {
	@Autowired
	private ModelMapper modelMapper;

	public TagDTO TagtoDTO(Tag tag) {
		return modelMapper.map(tag, TagDTO.class);
	}

	public Tag DTOtoTag(TagDTO tagDTO) {
		return modelMapper.map(tagDTO, Tag.class);
	}

	public List<TagDTO> TagListtoDTOList(List<Tag> tags) {
		return tags.stream()
				.map(this::TagtoDTO)
				.collect(Collectors.toList());
	}

	public void updateTagFromDTO(TagDTO dto, Tag tag) {
		modelMapper.map(dto, tag);
	}
}
