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
@Table(name = "group_members")
public class GroupMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    @Column
    private String displayName;
<<<<<<< Updated upstream

    @Column
    private Instant joinedAt = Instant.now();

=======
>>>>>>> Stashed changes
}
