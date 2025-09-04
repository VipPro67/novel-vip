package com.novel.vippro.Models;

<<<<<<< Updated upstream
import java.time.Instant;
import java.util.UUID;

=======
>>>>>>> Stashed changes
import jakarta.persistence.*;
import lombok.Data;
import com.novel.vippro.Models.base.BaseEntity;

@Data
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isRead = false;

<<<<<<< Updated upstream
    @Column(nullable = false)
     private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
=======
>>>>>>> Stashed changes
}
