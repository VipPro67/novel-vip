package com.novel.vippro.services;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.NovelMapper;
import com.novel.vippro.models.Novel;
import com.novel.vippro.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NovelService {

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private NovelMapper novelMapper;

    public Page<NovelDTO> getAllNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAll(pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getNovelsByCategory(String category, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByCategoriesContaining(category, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getNovelsByStatus(String status, Pageable pageable) {
        Page<Novel> novels = novelRepository.findByStatus(status, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> searchNovels(String keyword, Pageable pageable) {
        Page<Novel> novels = novelRepository.searchByKeyword(keyword, pageable);
        return novels.map(novelMapper::toDTO);
    }

    public NovelDTO getNovelById(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        return novelMapper.toDTO(novel);
    }

    public Page<NovelDTO> getHotNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByViewsDesc(pageable);
        return novels.map(novelMapper::toDTO);
    }

    public Page<NovelDTO> getTopRatedNovels(Pageable pageable) {
        Page<Novel> novels = novelRepository.findAllByOrderByRatingDesc(pageable);
        return novels.map(novelMapper::toDTO);
    }

    @Transactional
    public NovelDTO createNovel(Novel novel) {
        // Set default values
        novel.setViews(0);
        novel.setRating(0);
        novel.setTotalChapters(0);
        novel.setComments(null);

        Novel savedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(savedNovel);
    }

    @Transactional
    public NovelDTO updateNovel(UUID id, Novel novelDetails) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        // Update novel properties
        novel.setTitle(novelDetails.getTitle());
        novel.setSlug(novelDetails.getSlug());
        novel.setDescription(novelDetails.getDescription());
        novel.setAuthor(novelDetails.getAuthor());
        novel.setCoverImage(novelDetails.getCoverImage());
        novel.setStatus(novelDetails.getStatus());
        novel.setCategories(novelDetails.getCategories());

        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }

    @Transactional
    public void deleteNovel(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));
        novelRepository.delete(novel);
    }

    @Transactional
    public NovelDTO incrementViews(UUID id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setViews(novel.getViews() + 1);
        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }

    @Transactional
    public NovelDTO updateRating(UUID id, int rating) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", id));

        novel.setRating(rating);
        Novel updatedNovel = novelRepository.save(novel);
        return novelMapper.toDTO(updatedNovel);
    }
}