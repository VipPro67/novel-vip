package com.novel.vippro.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Group;
import com.novel.vippro.Models.GroupMember;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    @Query("SELECT g FROM Group g JOIN GroupMember gm ON g.id = gm.group.id WHERE gm.user.id = ?1")
    Page<Group> findGroupsByUserId(UUID userId, Pageable pageable);


    @Query("SELECT mg FROM GroupMember mg WHERE mg.group.id = ?1")
    List<GroupMember> findByGroupId(UUID groupId);

    @Query("SELECT mg FROM GroupMember mg WHERE mg.user.id = ?1 AND mg.group.id = ?2")
    GroupMember findByUserIdAndGroupId(UUID userId, UUID groupId);

    @Query("SELECT mg FROM GroupMember mg WHERE mg.user.id = ?1")
    List<GroupMember> findByUserId(UUID currentUserId);
}