package com.novel.vippro.Repository;

import com.novel.vippro.Models.UserDailyUsage;
import com.novel.vippro.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyUsageRepository extends JpaRepository<UserDailyUsage, UUID> {
    Optional<UserDailyUsage> findByUserAndDate(User user, LocalDate date);
}
