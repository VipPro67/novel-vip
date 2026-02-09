package com.novel.vippro.Models;

import java.time.Instant;
import java.util.UUID;

import com.novel.vippro.Models.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chapter_unlocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "chapter_id" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChapterUnlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(nullable = false)
    private Integer pricePaid;

    @Column(nullable = false)
    private Instant unlockedAt = Instant.now();
}
