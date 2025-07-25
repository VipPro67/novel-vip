package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.Models.Genre;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenreMapper {

	@Autowired
	private ModelMapper modelMapper;

	public GenreDTO GenretoDTO(Genre genre) {
		return modelMapper.map(genre, GenreDTO.class);
	}

	public Genre DTOtoGenre(GenreDTO genreDTO) {
		return modelMapper.map(genreDTO, Genre.class);
	}

	public List<GenreDTO> GenreListtoDTOList(List<Genre> genres) {
		return genres.stream()
				.map(this::GenretoDTO)
				.collect(Collectors.toList());
	}

	public void updateGenreFromDTO(GenreDTO dto, Genre genre) {
		modelMapper.map(dto, genre);
	}
}
