package com.novel.vippro.repository;

import com.novel.vippro.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.user.id = ?1 AND p.status = ?2 ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = ?1 AND p.status = ?2")
    long countByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = ?1 AND p.status = ?2")
    BigDecimal sumAmountByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN ?1 AND ?2")
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = ?1 GROUP BY p.paymentMethod")
    List<Object[]> countByPaymentMethodAndStatus(String status);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.status = ?1 GROUP BY p.paymentMethod")
    List<Object[]> sumAmountByPaymentMethodAndStatus(String status);

    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> countByStatus();
}