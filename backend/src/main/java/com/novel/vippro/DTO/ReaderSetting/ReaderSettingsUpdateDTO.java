package com.novel.vippro.DTO.ReaderSetting;

import lombok.Data;
import java.util.UUID;

@Data
public class ReaderSettingsUpdateDTO {
    private UUID userId;
    private Integer fontSize;
    private String fontFamily;
    private Double lineHeight;
    private String theme;
    private Integer marginSize;
    private Integer paragraphSpacing;
    private Boolean autoScroll;
    private Integer autoScrollSpeed;
    private Boolean keepScreenOn;
    private Boolean showProgress;
    private Boolean showChapterTitle;
    private Boolean showTime;
    private Boolean showBattery;
    private String textColor;
    private String backgroundColor;
    private Boolean audioEnabled;
    private Boolean audioAutoNextChapter;
    private Double audioSpeed;
}
