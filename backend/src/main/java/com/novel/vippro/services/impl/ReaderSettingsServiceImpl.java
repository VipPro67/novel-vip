package com.novel.vippro.services.impl;

import com.novel.vippro.dto.ReaderSettingsDTO;
import com.novel.vippro.dto.ReaderSettingsUpdateDTO;
import com.novel.vippro.models.ReaderSettings;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.ReaderSettingsRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.ReaderSettingsService;
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
    private final UserRepository userRepository;

    @Override
    public ReaderSettingsDTO getUserSettings() {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        return convertToDTO(settings);
    }

    @Override
    @Transactional
    public ReaderSettingsDTO updateSettings(ReaderSettingsUpdateDTO settingsDTO) {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        updateSettingsFromDTO(settings, settingsDTO);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return convertToDTO(savedSettings);
    }

    @Override
    @Transactional
    public ReaderSettingsDTO resetSettings() {
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder

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
        return convertToDTO(savedSettings);
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
        // TODO: Get current user ID from security context
        UUID currentUserId = UUID.randomUUID(); // Placeholder

        ReaderSettings settings = readerSettingsRepository.findByUserId(currentUserId)
                .orElseGet(() -> createDefaultSettings(currentUserId));

        settings.setTheme(themeId);

        ReaderSettings savedSettings = readerSettingsRepository.save(settings);
        return convertToDTO(savedSettings);
    }

    private ReaderSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ReaderSettings settings = new ReaderSettings();
        settings.setUser(user);
        // Default values are already set in the entity

        return readerSettingsRepository.save(settings);
    }

    private void updateSettingsFromDTO(ReaderSettings settings, ReaderSettingsUpdateDTO dto) {
        if (dto.getFontSize() != null)
            settings.setFontSize(dto.getFontSize());
        if (dto.getFontFamily() != null)
            settings.setFontFamily(dto.getFontFamily());
        if (dto.getLineHeight() != null)
            settings.setLineHeight(dto.getLineHeight());
        if (dto.getTheme() != null)
            settings.setTheme(dto.getTheme());
        if (dto.getMarginSize() != null)
            settings.setMarginSize(dto.getMarginSize());
        if (dto.getParagraphSpacing() != null)
            settings.setParagraphSpacing(dto.getParagraphSpacing());
        if (dto.getAutoScroll() != null)
            settings.setAutoScroll(dto.getAutoScroll());
        if (dto.getAutoScrollSpeed() != null)
            settings.setAutoScrollSpeed(dto.getAutoScrollSpeed());
        if (dto.getKeepScreenOn() != null)
            settings.setKeepScreenOn(dto.getKeepScreenOn());
        if (dto.getShowProgress() != null)
            settings.setShowProgress(dto.getShowProgress());
        if (dto.getShowChapterTitle() != null)
            settings.setShowChapterTitle(dto.getShowChapterTitle());
        if (dto.getShowTime() != null)
            settings.setShowTime(dto.getShowTime());
        if (dto.getShowBattery() != null)
            settings.setShowBattery(dto.getShowBattery());
    }

    private ReaderSettingsDTO convertToDTO(ReaderSettings settings) {
        ReaderSettingsDTO dto = new ReaderSettingsDTO();
        dto.setId(settings.getId());
        dto.setUserId(settings.getUser().getId());
        dto.setFontSize(settings.getFontSize());
        dto.setFontFamily(settings.getFontFamily());
        dto.setLineHeight(settings.getLineHeight());
        dto.setTheme(settings.getTheme());
        dto.setMarginSize(settings.getMarginSize());
        dto.setParagraphSpacing(settings.getParagraphSpacing());
        dto.setAutoScroll(settings.getAutoScroll());
        dto.setAutoScrollSpeed(settings.getAutoScrollSpeed());
        dto.setKeepScreenOn(settings.getKeepScreenOn());
        dto.setShowProgress(settings.getShowProgress());
        dto.setShowChapterTitle(settings.getShowChapterTitle());
        dto.setShowTime(settings.getShowTime());
        dto.setShowBattery(settings.getShowBattery());
        return dto;
    }
}