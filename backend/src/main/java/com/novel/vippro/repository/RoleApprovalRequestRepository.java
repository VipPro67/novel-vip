package com.novel.vippro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.models.RoleApprovalRequest;
import com.novel.vippro.models.User;

@Repository
public interface RoleApprovalRequestRepository extends JpaRepository<RoleApprovalRequest, UUID> {
    List<RoleApprovalRequest> findByStatus(String status);

    List<RoleApprovalRequest> findByUser(User user);

    Optional<RoleApprovalRequest> findByUserAndStatus(User user, String status);
}