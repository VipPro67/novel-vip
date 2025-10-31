package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view_stats")
public class ViewStat extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "view_date", nullable = false)
    private LocalDateTime viewDate = LocalDateTime.now();

    public Novel getNovel() {
        return novel;
    }

    public void setNovel(Novel novel) {
        this.novel = novel;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDateTime viewDate) {
        this.viewDate = viewDate;
    }
}
