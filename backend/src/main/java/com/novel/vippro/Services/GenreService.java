package com.novel.vippro.Services;

import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Repository.GenreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private Mapper mapper;

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreDTO createGenre(GenreDTO genreDTO) {
        Genre genre = mapper.DTOtoGenre(genreDTO);
        Genre savedGenre = genreRepository.save(genre);
        return mapper.GenretoDTO(savedGenre);
    }

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreDTO updateGenre(UUID id, GenreDTO genreDTO) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        genre.setName(genreDTO.getName());
        Genre updatedGenre = genreRepository.save(genre);
        return mapper.GenretoDTO(updatedGenre);
    }

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public void deleteGenre(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        genreRepository.delete(genre);
    }

    @Cacheable(value = "genres", key = "#id")
    public GenreDTO getGenreById(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        return mapper.GenretoDTO(genre);
    }

    @Cacheable(value = "genres", key = "#name")
    public GenreDTO getGenreByName(String name) {
        Genre genre = genreRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "name", name));
        return mapper.GenretoDTO(genre);
    }

    @Cacheable(value = "genres")
    public List<GenreDTO> getAllGenres() {
        List<GenreDTO> genreDTOs = new ArrayList<>();
        for (Genre genre : genreRepository.findAll()) {
            genreDTOs.add(mapper.GenretoDTO(genre));
        }
        return genreDTOs;
    }
}