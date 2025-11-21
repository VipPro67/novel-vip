package com.novel.vippro.Models;

import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coin_transactions")
@Getter
@Setter
@NoArgsConstructor
public class CoinTransaction extends BaseEntity {

    public enum TransactionType {
        UNLOCK_CHAPTER,
        DAILY_REWARD,
        ADMIN_ADJUST,
        WITHDRAW,
        DEPOSIT,
        AUDIO_EXTRA
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionType type;
}

