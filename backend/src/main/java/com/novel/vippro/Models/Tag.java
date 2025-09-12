package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = true)
    private String description;

    public Tag(String name) {
        this.name = name;
    }

}