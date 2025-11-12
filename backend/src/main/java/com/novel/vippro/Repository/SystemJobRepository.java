package com.novel.vippro.Repository;

import com.novel.vippro.Models.SystemJob;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemJobRepository extends JpaRepository<SystemJob, UUID> {

    List<SystemJob> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<SystemJob> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
