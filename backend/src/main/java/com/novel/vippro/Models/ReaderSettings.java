package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "reader_settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReaderSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "font_size", nullable = false)
    private Integer fontSize = 16;

    @Column(name = "font_family", nullable = false)
    private String fontFamily = "Arial";

    @Column(name = "line_height", nullable = false)
    private Double lineHeight = 1.5;

    @Column(name = "theme", nullable = false)
    private String theme = "light";

    @Column(name = "margin_size", nullable = false)
    private Integer marginSize = 20;

    @Column(name = "paragraph_spacing", nullable = false)
    private Integer paragraphSpacing = 10;

    @Column(name = "auto_scroll", nullable = false)
    private Boolean autoScroll = false;

    @Column(name = "auto_scroll_speed", nullable = false)
    private Integer autoScrollSpeed = 1;

    @Column(name = "keep_screen_on", nullable = false)
    private Boolean keepScreenOn = true;

    @Column(name = "show_progress", nullable = false)
    private Boolean showProgress = true;

    @Column(name = "show_chapter_title", nullable = false)
    private Boolean showChapterTitle = true;

    @Column(name = "show_time", nullable = false)
    private Boolean showTime = true;

    @Column(name = "show_battery", nullable = false)
    private Boolean showBattery = true;

    @Column(name = "text_color", nullable = false, length = 9, columnDefinition = "VARCHAR(9) DEFAULT '#000000'")
    private String textColor = "#000000";

    @Column(name = "background_color", nullable = false, length = 9, columnDefinition = "VARCHAR(9) DEFAULT '#FFFFFF'")
    private String backgroundColor = "#FFFFFF";

    @Column(name = "audio_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean audioEnabled = false;

    @Column(name = "audio_auto_next_chapter", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean audioAutoNextChapter = false;

    @Column(name = "audio_speed", nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 1.0")
    private Double audioSpeed = 1.0;

    @Column(name = "correction_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean correctionEnabled = false;
}
