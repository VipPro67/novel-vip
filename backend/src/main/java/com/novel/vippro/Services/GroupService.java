package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Group.CreateGroupDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.Group.UpdateGroupDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Group;
import com.novel.vippro.Models.GroupMember;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.GroupMemberRepository;
import com.novel.vippro.Repository.GroupRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.GroupService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private Mapper mapper;

    @Cacheable(value = "groups", key = "#pageable")
    public List<GroupDTO> getAllGroups(Pageable pageable) {
        Page<Group> groupPage = groupRepository.findAll(pageable);
        List<GroupDTO> groupDTOs = groupPage.getContent().stream()
                .map(mapper::GroupToDTO)
                .collect(Collectors.toList());
        return groupDTOs;
    }

    @Cacheable(value = "groups", key = "#id")
    public GroupDTO getGroupById(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return mapper.GroupToDTO(group);
    }

    @Transactional
    public GroupDTO createGroup(CreateGroupDTO groupDTO) {
        Group group = mapper.CreateDTOtoGroup(groupDTO);
        group = groupRepository.save(group);
        groupRepository.flush();
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setIsAdmin(true);
        groupMember.setDisplayName(user.getFullName());
        groupMember = groupMemberRepository.save(groupMember);
        groupMemberRepository.save(groupMember);
        return mapper.GroupToDTO(group);
    }
    @Transactional
    @CacheEvict(value = "groups", key = "#id")
    public GroupDTO updateGroup(UUID id, UpdateGroupDTO groupDTO) {
        Group existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        mapper.updateGroupFromDTO(groupDTO, existingGroup);
        existingGroup = groupRepository.save(existingGroup);
        return mapper.GroupToDTO(existingGroup);
    }
    @Transactional
    @CacheEvict(value = "groups", key = "#id")
    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getMyGroups() {
        UUID currentUserId = UserDetailsImpl.getCurrentUserId();
        List<GroupMember> groupMembers = groupMemberRepository.findByUserId(currentUserId);
        List<GroupDTO> groupDTOs = groupMembers.stream()
                .map(GroupMember::getGroup)
                .map(mapper::GroupToDTO)
                .collect(Collectors.toList());
        return groupDTOs;
    }

}