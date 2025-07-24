package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Chapter.ChapterListDTO;
import com.novel.vippro.Models.Chapter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChapterMapper {

	@Autowired
	private ModelMapper modelMapper;

	public ChapterDTO ChaptertoDTO(Chapter chapter) {
		return modelMapper.map(chapter, ChapterDTO.class);
	}

	public ChapterDetailDTO ChaptertoChapterDetailDTO(Chapter chapter) {
		ChapterDetailDTO chapterDetailDTO = modelMapper.map(chapter, ChapterDetailDTO.class);
		chapterDetailDTO.setNovelId(chapter.getNovel().getId());
		chapterDetailDTO.setNovelTitle(chapter.getNovel().getTitle());
		return chapterDetailDTO;
	}

	public ChapterDTO ChaptertoChapterDTO(Chapter chapter) {
		return modelMapper.map(chapter, ChapterDTO.class);
	}

	public ChapterListDTO ChaptertoChapterListDTO(Chapter chapter) {
		ChapterListDTO chapterListDTO = modelMapper.map(chapter, ChapterListDTO.class);
		chapterListDTO.setNovelId(chapter.getNovel().getId());
		chapterListDTO.setNovelTitle(chapter.getNovel().getTitle());
		return chapterListDTO;
	}

	public List<ChapterDTO> ChapterListtoDTOList(List<Chapter> chapters) {
		return chapters.stream()
				.map(this::ChaptertoDTO)
				.collect(Collectors.toList());
	}

	public void updateChapterFromDTO(ChapterDTO dto, Chapter chapter) {
		modelMapper.map(dto, chapter);
	}
}