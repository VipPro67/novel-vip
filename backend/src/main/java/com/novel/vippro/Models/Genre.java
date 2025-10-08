package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "genres")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Genre extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = true)
    private String description;

    public Genre(String name) {
        this.name = name;
    }
}