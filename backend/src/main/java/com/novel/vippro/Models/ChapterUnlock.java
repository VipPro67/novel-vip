package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chapter_unlocks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chapter_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ChapterUnlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
}

