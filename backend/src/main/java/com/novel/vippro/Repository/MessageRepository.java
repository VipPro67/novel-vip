package com.novel.vippro.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("SELECT m FROM Message m WHERE (m.sender.id = ?1 OR m.receiver.id = ?1) AND m.group IS NULL")
    List<Message> findBySenderOrReceiver(UUID userId);

    @Query("SELECT m FROM Message m WHERE m.group.id IN (SELECT g.id FROM Group g JOIN GroupMember gm ON g.id = gm.group.id WHERE gm.user.id = ?1) AND m.group IS NOT NULL")
    List<Message> findByGroupMembers(UUID userId);

    @Query("SELECT m FROM Message m WHERE m.content LIKE %?1%")
    List<Message> findByContentContaining(String content);

    @Query("SELECT m FROM Message m WHERE (m.group.id = ?1 OR m.receiver.id = ?1) AND m.group IS NOT NULL")
    List<Message> findByGroupOrReceiver(UUID id);
}