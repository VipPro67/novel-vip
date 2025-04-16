package com.novel.vippro.repository;

import com.novel.vippro.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserIdAndStatus(UUID userId, String status);

    Page<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}