package com.novel.vippro.repository;

import com.novel.vippro.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameIgnoreCase(String name);
}