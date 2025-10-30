package com.novel.vippro.Services;

import com.novel.vippro.DTO.Notification.CreateNotificationDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.Exception.BadRequestException;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Favorite;
import com.novel.vippro.Models.NotificationType;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.FavoriteRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;

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
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<NovelDTO> getUserFavorites(Pageable pageable) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        var favoritesNovel = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return new PageResponse<>(favoritesNovel.map(mapper::NoveltoDTO));
    }

    @Transactional
    public void addToFavorites(UUID novelId) {
        UUID userId = UserDetailsImpl.getCurrentUserId();

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
        UUID userId = UserDetailsImpl.getCurrentUserId();

        if (!favoriteRepository.existsByUserIdAndNovelId(userId, novelId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }

        favoriteRepository.deleteByUserIdAndNovelId(userId, novelId);
    }

    public boolean isFavorite(UUID novelId) {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        return favoriteRepository.existsByUserIdAndNovelId(userId, novelId);
    }

    public long getFavoriteCount(UUID novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel", "id", novelId);
        }
        return favoriteRepository.countByNovelId(novelId);
    }

    @Transactional
    public void notifyFavorites(UUID novelId) {
        var favorites = favoriteRepository.findByNovelId(novelId);
        if (favorites.isEmpty()) {
            return; 
        }
        var novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", novelId));
        for (Favorite favorite : favorites) {
            User user = favorite.getUser();
            if (user != null) {
                CreateNotificationDTO notificationDTO = new CreateNotificationDTO();
                notificationDTO.setUserId(user.getId());
                notificationDTO.setTitle("New Chapter Available");
                notificationDTO.setMessage("A new chapter has been added to novel: " + novel.getTitle());
                notificationDTO.setType(NotificationType.CHAPTER_UPDATE);
                notificationDTO.setReference(novel.getSlug());
                notificationService.createNotification(notificationDTO);
            }
        }
        
    }
}