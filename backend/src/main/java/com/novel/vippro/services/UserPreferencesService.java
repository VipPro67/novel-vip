package com.novel.vippro.services;

import com.novel.vippro.models.UserPreferences;
import java.util.UUID;

public interface UserPreferencesService {
    UserPreferences getUserPreferences(UUID userId);

    UserPreferences updateUserPreferences(UUID userId, UserPreferences preferences);

    void deleteUserPreferences(UUID userId);
}