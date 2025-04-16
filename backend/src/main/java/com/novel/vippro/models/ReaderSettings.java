package com.novel.vippro.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "reader_settings")
@Data
public class ReaderSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

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
}