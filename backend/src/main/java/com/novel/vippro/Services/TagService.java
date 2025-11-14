package com.novel.vippro.Services;

import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Tag;
import com.novel.vippro.Repository.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private Mapper mapper;

    @CacheEvict(value = "tags", allEntries = true)
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        Tag tag = mapper.DTOtoTag(tagDTO);
        Tag savedTag = tagRepository.save(tag);
        return mapper.TagtoDTO(savedTag);
    }

    @CacheEvict(value = "tags", allEntries = true)
    @Transactional
    public TagDTO updateTag(UUID id, TagDTO tagDTO) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        tag.setName(tagDTO.name());
        Tag updatedTag = tagRepository.save(tag);
        return mapper.TagtoDTO(updatedTag);
    }

    @CacheEvict(value = "tags", allEntries = true)
    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        tagRepository.delete(tag);
    }

    @Cacheable(value = "tags", key = "#id")
    public TagDTO getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        return mapper.TagtoDTO(tag);
    }

    @Cacheable(value = "tags", key = "#name")
    public TagDTO getTagByName(String name) {
        Tag tag = tagRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "name", name));
        return mapper.TagtoDTO(tag);
    }

    @Cacheable(value = "tags")
    public List<TagDTO> getAllTags() {
        List<TagDTO> tagDTOs = new ArrayList<>();
        for (Tag tag : tagRepository.findAll()) {
            tagDTOs.add(mapper.TagtoDTO(tag));
        }
        return tagDTOs;
    }
}