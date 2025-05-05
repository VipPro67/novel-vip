package com.novel.vippro.Services;

import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.ReaderSettings;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.ReaderSettingsRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Services.ReaderSettingsService;
import com.novel.vippro.Services.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ReaderSettingsService {

    @Autowired
    private ReaderSettingsRepository readerSettingsRepository;
    @Autowired
    private Mapper mapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    public ReaderSettingsDTO getUserSettings() {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        return mapper.ReaderSettingsToReaderSettingsDTO(settings);
    }

    @Transactional
    public ReaderSettingsDTO updateSettings(ReaderSettingsUpdateDTO settingsDTO) {
        UUID currentUserId = userService.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        mapper.updateReaderSettingsFromDTO(settingsDTO, settings);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return mapper.ReaderSettingsToReaderSettingsDTO(savedSettings);
    }

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

    public List<String> getFontOptions() {
        return Arrays.asList("Arial", "Times New Roman", "Courier New", "Georgia", "Verdana", "Helvetica");
    }

    public List<String> getThemeOptions() {
        return Arrays.asList("light", "dark", "sepia", "night", "custom");
    }

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