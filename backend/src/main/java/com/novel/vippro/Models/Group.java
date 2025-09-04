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
@Table(name = "groups")
public class Group extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

<<<<<<< Updated upstream
    @Column(nullable = false)
     private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
=======
>>>>>>> Stashed changes
}
