package com.novel.vippro.repository;

import com.novel.vippro.models.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByPlanId(String planId);

    List<SubscriptionPlan> findByActiveTrue();
}