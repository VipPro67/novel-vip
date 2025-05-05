package com.novel.vippro.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
}