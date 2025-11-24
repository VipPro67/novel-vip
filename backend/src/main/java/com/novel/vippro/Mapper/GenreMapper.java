package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.Models.Genre;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface GenreMapper {

    GenreDTO GenretoDTO(Genre genre);

    Genre DTOtoGenre(GenreDTO genreDTO);

    List<GenreDTO> GenreListtoDTOList(List<Genre> genres);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateGenreFromDTO(GenreDTO dto, @MappingTarget Genre genre);
}
