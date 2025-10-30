package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.Instant;
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

    private int lastReadChapterIndex;

    @Column(name = "last_read_at")
    @UpdateTimestamp
    private Instant lastReadAt;
}