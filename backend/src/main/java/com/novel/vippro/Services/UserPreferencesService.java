package com.novel.vippro.Services;

import com.novel.vippro.Models.UserPreferences;
import com.novel.vippro.Repository.UserPreferencesRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Services.UserPreferencesService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    public UserPreferences getUserPreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User preferences not found"));
    }

    public UserPreferences updateUserPreferences(UUID userId, UserPreferences preferences) {
        UserPreferences existingPreferences = userPreferencesRepository.findByUserId(userId)
                .orElse(new UserPreferences());

        existingPreferences.setUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
        existingPreferences.setFavoriteGenres(preferences.getFavoriteGenres());
        existingPreferences.setFavoriteTags(preferences.getFavoriteTags());
        existingPreferences.setPreferredLanguage(preferences.getPreferredLanguage());
        existingPreferences.setPreferredStatus(preferences.getPreferredStatus());
        existingPreferences.setMinRating(preferences.getMinRating());
        existingPreferences.setMaxChapters(preferences.getMaxChapters());

        return userPreferencesRepository.save(existingPreferences);
    }

    public void deleteUserPreferences(UUID userId) {
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User preferences not found"));
        userPreferencesRepository.delete(preferences);
    }
}