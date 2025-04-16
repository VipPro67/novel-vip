package com.novel.vippro.services;

import com.novel.vippro.dto.ReaderSettingsDTO;
import com.novel.vippro.dto.ReaderSettingsUpdateDTO;
import java.util.List;

public interface ReaderSettingsService {
    ReaderSettingsDTO getUserSettings();

    ReaderSettingsDTO updateSettings(ReaderSettingsUpdateDTO settingsDTO);

    ReaderSettingsDTO resetSettings();

    List<String> getFontOptions();

    List<String> getThemeOptions();

    ReaderSettingsDTO applyTheme(String themeId);
}