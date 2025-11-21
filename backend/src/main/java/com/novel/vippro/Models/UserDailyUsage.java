package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_daily_usage")
@Getter
@Setter
@NoArgsConstructor
public class UserDailyUsage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "audio_generated_count")
    private Integer audioGeneratedCount = 0;

    @Column(name = "chapters_read_count")
    private Integer chaptersReadCount = 0;
}

