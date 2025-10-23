package com.novel.vippro.Repository;

import com.novel.vippro.Models.EpubImportJob;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpubImportJobRepository extends JpaRepository<EpubImportJob, UUID> {
    List<EpubImportJob> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<EpubImportJob> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
