package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.novel.vippro.Models.base.BaseEntity;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reading_history")
public class ReadingHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "progress")
    private Integer progress; 

    @Column(name = "reading_time")
    private Integer readingTime;

    @Column(name = "last_read_at")
    @UpdateTimestamp
    private Instant lastReadAt;
}