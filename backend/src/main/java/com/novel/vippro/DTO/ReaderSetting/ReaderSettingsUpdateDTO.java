package com.novel.vippro.DTO.ReaderSetting;

import lombok.Builder;
import java.util.UUID;

@Builder
public record ReaderSettingsUpdateDTO(
    UUID userId,
    Integer fontSize,
    String fontFamily,
    Double lineHeight,
    String theme,
    Integer marginSize,
    Integer paragraphSpacing,
    Boolean autoScroll,
    Integer autoScrollSpeed,
    Boolean keepScreenOn,
    Boolean showProgress,
    Boolean showChapterTitle,
    Boolean showTime,
    Boolean showBattery,
    String textColor,
    String backgroundColor,
    Boolean audioEnabled,
    Boolean audioAutoNextChapter,
    Double audioSpeed,
    Boolean correctionEnabled
) {}
