package com.novel.vippro.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.ReaderSettings;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReaderSettingsRepository extends JpaRepository<ReaderSettings, UUID> {
    Optional<ReaderSettings> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}