package com.novel.vippro.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByUsername(String username);

        Boolean existsByUsername(String username);

        Boolean existsByEmail(String email);

        // get user by email with all user roles and permissions
        @Query("SELECT DISTINCT u FROM User u " +
                        "LEFT JOIN FETCH u.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE u.email = :email")
        Optional<User> findByEmail(String email);

        @Query("SELECT DISTINCT u FROM User u " +
                        "LEFT JOIN FETCH u.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE u.username = :username")
        Optional<User> findByUsernameWithRoles(String username);

        @Query("SELECT u FROM User u WHERE " +
                        "(:username IS NULL OR username ILIKE CONCAT('%', :username, '%')) AND " +
                        "(:email IS NULL OR email ILIKE CONCAT('%', :email, '%')) AND " +
                        "(:fullName IS NULL OR fullName ILIKE CONCAT('%', :fullName, '%'))")
        Page<User> searchUsers(
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("fullName") String fullName,
                        Pageable pageable);

        Optional<User> findByEmailVerificationToken(String emailVerificationToken);
}
