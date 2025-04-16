package com.novel.vippro.repository;

import com.novel.vippro.models.User;
import com.novel.vippro.models.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
    Optional<UserPreferences> findByUser(User user);

    @Query("SELECT up FROM UserPreferences up WHERE up.user.id = :userId")
    Optional<UserPreferences> findByUserId(@Param("userId") UUID userId);
}