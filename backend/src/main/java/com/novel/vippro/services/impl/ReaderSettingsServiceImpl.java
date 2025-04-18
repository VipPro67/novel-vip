package com.novel.vippro.services.impl;

import com.novel.vippro.dto.ReaderSettingsDTO;
import com.novel.vippro.dto.ReaderSettingsUpdateDTO;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.models.ReaderSettings;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.ReaderSettingsRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.ReaderSettingsService;
import com.novel.vippro.services.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReaderSettingsServiceImpl implements ReaderSettingsService {

    private final ReaderSettingsRepository readerSettingsRepository;
    private final Mapper mapper;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public ReaderSettingsDTO getUserSettings() {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        return mapper.ReaderSettingsToReaderSettingsDTO(settings);
    }

    @Override
    @Transactional
    public ReaderSettingsDTO updateSettings(ReaderSettingsUpdateDTO settingsDTO) {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        mapper.updateReaderSettingsFromDTO(settingsDTO, settings);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return mapper.ReaderSettingsToReaderSettingsDTO(savedSettings);
    }

    @Override
    @Transactional
    public ReaderSettingsDTO resetSettings() {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        // Reset to default values
        settings.setFontSize(16);
        settings.setFontFamily("Arial");
        settings.setLineHeight(1.5);
        settings.setTheme("light");
        settings.setMarginSize(20);
        settings.setParagraphSpacing(10);
        settings.setAutoScroll(false);
        settings.setAutoScrollSpeed(1);
        settings.setKeepScreenOn(true);
        settings.setShowProgress(true);
        settings.setShowChapterTitle(true);
        settings.setShowTime(true);
        settings.setShowBattery(true);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return mapper.ReaderSettingsToReaderSettingsDTO(savedSettings);
    }

    @Override
    public List<String> getFontOptions() {
        return Arrays.asList("Arial", "Times New Roman", "Courier New", "Georgia", "Verdana", "Helvetica");
    }

    @Override
    public List<String> getThemeOptions() {
        return Arrays.asList("light", "dark", "sepia", "night", "custom");
    }

    @Override
    @Transactional
    public ReaderSettingsDTO applyTheme(String themeId) {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        settings.setTheme(themeId);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return mapper.ReaderSettingsToReaderSettingsDTO(savedSettings);
    }

    private ReaderSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ReaderSettings settings = new ReaderSettings();
        settings.setUser(user);
        // Default values are already set in the entity

        return readerSettingsRepository.save(settings);
    }

}