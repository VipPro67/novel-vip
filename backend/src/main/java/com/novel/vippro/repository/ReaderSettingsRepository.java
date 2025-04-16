package com.novel.vippro.repository;

import com.novel.vippro.models.ReaderSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReaderSettingsRepository extends JpaRepository<ReaderSettings, UUID> {
    Optional<ReaderSettings> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}