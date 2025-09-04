package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;
<<<<<<< Updated upstream
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
=======
import com.novel.vippro.Models.base.BaseEntity;
>>>>>>> Stashed changes

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "novel_id" })
}, indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_novel_id", columnList = "novel_id")
})
@Data
public class Favorite extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
<<<<<<< Updated upstream

    @CreationTimestamp
     private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
=======
>>>>>>> Stashed changes
}