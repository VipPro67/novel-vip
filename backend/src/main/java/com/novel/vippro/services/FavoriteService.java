package com.novel.vippro.services;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.exception.BadRequestException;
import com.novel.vippro.models.Favorite;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.FavoriteRepository;
import com.novel.vippro.repository.NovelRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.mapper.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    private UserService userService;

    public PageResponse<NovelDTO> getUserFavorites(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        return new PageResponse<>(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(favorite -> mapper.NoveltoDTO(favorite.getNovel())));
    }

    @Transactional
    public void addToFavorites(UUID novelId) {
        UUID userId = userService.getCurrentUserId();

        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndNovelId(userId, novelId)) {
            throw new BadRequestException("Novel is already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setNovel(novel);

        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(UUID novelId) {
        UUID userId = userService.getCurrentUserId();

        if (!favoriteRepository.existsByUserIdAndNovelId(userId, novelId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }

        favoriteRepository.deleteByUserIdAndNovelId(userId, novelId);
    }

    public boolean isFavorite(UUID novelId) {
        UUID userId = userService.getCurrentUserId();
        return favoriteRepository.existsByUserIdAndNovelId(userId, novelId);
    }

    public long getFavoriteCount(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return favoriteRepository.countByNovelId(novelId);
    }
}