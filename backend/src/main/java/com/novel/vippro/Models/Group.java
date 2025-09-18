package com.novel.vippro.Models;

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

}
