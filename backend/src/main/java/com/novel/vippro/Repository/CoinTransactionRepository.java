package com.novel.vippro.Repository;

import com.novel.vippro.Models.CoinTransaction;
import com.novel.vippro.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, UUID> {
    List<CoinTransaction> findByUserOrderByCreatedAtDesc(User user);
}
