package com.novel.vippro.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Role;
import com.novel.vippro.Models.RoleApprovalRequest;
import com.novel.vippro.Models.User;

@Repository
public interface RoleApprovalRequestRepository extends JpaRepository<RoleApprovalRequest, UUID> {
    @Query("SELECT r FROM RoleApprovalRequest r WHERE r.status = ?1")
    Page<RoleApprovalRequest> findByStatus(String status, Pageable pageable);

    List<RoleApprovalRequest> findByUser(User user);

    @Query("SELECT r FROM RoleApprovalRequest r WHERE r.user = ?1 AND r.status = ?2 AND r.requestedRole = ?3")
    Optional<RoleApprovalRequest> findByUserAndStatus(User user, String status, Role requestedRole);
}