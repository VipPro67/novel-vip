package com.novel.vippro.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false, unique = true)
  private ERole name;

  @Column(length = 255)
  private String description;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();
}
