package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.GroupMember;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.GroupMemberRepository;

@Service
public class GroupMemberService {
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private Mapper mapper;

    @Cacheable(value = "groupMembers", key = "#pageable")
    public PageResponse<GroupMemberDTO> getAllGroupMembers(Pageable pageable) {
        Page<GroupMember> groupMemberPage = groupMemberRepository.findAll(pageable);
        PageResponse<GroupMemberDTO> groupMemberDTOs = new PageResponse<>();
        groupMemberDTOs.setContent(groupMemberPage.getContent().stream()
                .map(mapper::GroupMembertoDTO)
                .toList());
        return groupMemberDTOs;
    }

    public GroupMemberDTO addGroupMember(UUID groupId, GroupMemberDTO groupMemberDTO) {
        GroupMember currentUserGroupMember = groupMemberRepository.findByUserIdAndGroupId(
                userService.getCurrentUser().getId(), groupId);
        if (currentUserGroupMember == null || !currentUserGroupMember.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to add members to this group");
        }
        GroupMember existingGroupMember = groupMemberRepository.findByUserIdAndGroupId(groupMemberDTO.getUserId(),
                groupId);
        if (existingGroupMember != null) {
            throw new RuntimeException("User is already a member of the group");
        }

        GroupMember groupMember = mapper.DTOtoGroupMember(groupMemberDTO);
        GroupDTO group = groupService.getGroupById(groupId);
        User user = userService.getUserById(groupMemberDTO.getUserId());
        groupMember.setGroup(mapper.DTOtoGroup(group));
        groupMember.setUser(user);
        groupMember.setIsAdmin(groupMemberDTO.getIsAdmin());
        groupMember.setDisplayName(groupMemberDTO.getDisplayName());
        groupMember = groupMemberRepository.save(groupMember);
        return mapper.GroupMembertoDTO(groupMember);
    }

    @CacheEvict(value = "groupMembers", key = "#groupId")
    public void removeGroupMember(UUID groupId, UUID userId) {
        GroupMember currentUserGroupMember = groupMemberRepository.findByUserIdAndGroupId(
                userService.getCurrentUser().getId(), groupId);
        if (currentUserGroupMember == null || !currentUserGroupMember.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to remove members from this group");
        }
        GroupMember existingGroupMember = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
        if (existingGroupMember == null) {
            throw new RuntimeException("User is not a member of the group");
        }
        groupMemberRepository.delete(existingGroupMember);
    }

    @CacheEvict(value = "groupMembers", key = "#groupId")
    public void leaveGroup(UUID groupId) {
        GroupMember existingGroupMember = groupMemberRepository.findByUserIdAndGroupId(
                userService.getCurrentUser().getId(), groupId);
        if (existingGroupMember == null) {
            throw new RuntimeException("You are not a member of this group");
        }
        groupMemberRepository.delete(existingGroupMember);
    }

    @Cacheable(value = "groupMembersByGroupId", key = "#groupId")
    public List<GroupMemberDTO> getMembersByGroupId(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(mapper::GroupMembertoDTO)
                .toList();
    }

    @Cacheable(value = "groupMember", key = "#id")
    public GroupMemberDTO getGroupMemberById(UUID id) {
        GroupMember groupMember = groupMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupMember not found"));
        return mapper.GroupMembertoDTO(groupMember);
    }

    
    public GroupMemberDTO createGroupMember(GroupMemberDTO groupMemberDTO) {
        GroupMember groupMember = mapper.DTOtoGroupMember(groupMemberDTO);
        groupMember = groupMemberRepository.save(groupMember);
        return mapper.GroupMembertoDTO(groupMember);
    }

    public GroupMemberDTO updateGroupMember(UUID id, GroupMemberDTO groupMemberDTO) {
        GroupMember existingGroupMember = groupMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupMember not found"));
        mapper.updateGroupMemberFromDTO(groupMemberDTO, existingGroupMember);
        existingGroupMember = groupMemberRepository.save(existingGroupMember);
        return mapper.GroupMembertoDTO(existingGroupMember);
    }

    public void deleteGroupMember(UUID id) {
        groupMemberRepository.deleteById(id);
    }
}