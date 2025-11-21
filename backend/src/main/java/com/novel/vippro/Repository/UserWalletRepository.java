package com.novel.vippro.Repository;

import com.novel.vippro.Models.UserWallet;
import com.novel.vippro.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID> {
    Optional<UserWallet> findByUser(User user);
}
