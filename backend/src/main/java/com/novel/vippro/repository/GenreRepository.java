package com.novel.vippro.repository;

import com.novel.vippro.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {
    @Query("SELECT g FROM Genre g WHERE LOWER(g.name) = LOWER(:name)")
    Optional<Genre> findByNameIgnoreCase(String name);
}