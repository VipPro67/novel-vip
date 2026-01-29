package com.novel.vippro.Repository;

import com.novel.vippro.Models.User;
import com.novel.vippro.Models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUser(User user);
    Optional<UserAchievement> findByUserAndAchievementCode(User user, String code);
    List<UserAchievement> findByUserAndIsEquippedTrue(User user);
}
