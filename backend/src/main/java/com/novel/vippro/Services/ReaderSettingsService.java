package com.novel.vippro.Services;

import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.ReaderSettings;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.ReaderSettingsRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.ReaderSettingsService;

import jakarta.persistence.EntityNotFoundException;

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
    private UserRepository userRepository;

    public ReaderSettingsDTO getUserSettings() {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        return mapper.ReaderSettingsToReaderSettingsDTO(settings);
    }

    @Transactional
    public ReaderSettingsDTO updateSettings(ReaderSettingsUpdateDTO settingsDTO) {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        mapper.updateReaderSettingsFromDTO(settingsDTO, settings);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return mapper.ReaderSettingsToReaderSettingsDTO(savedSettings);
    }

    @Transactional
    public ReaderSettingsDTO resetSettings() {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

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
        settings.setTextColor("#000000");
        settings.setBackgroundColor("#FFFFFF");
        settings.setAudioEnabled(false);
        settings.setAudioAutoNextChapter(false);
        settings.setAudioSpeed(1.0);

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
        UUID userId = UserDetailsImpl.getCurrentUserId();

        ReaderSettings settings = readerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

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
