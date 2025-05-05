package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Group;
import com.novel.vippro.Models.User;
import com.novel.vippro.Repository.GroupRepository;
import com.novel.vippro.Services.GroupService;

import jakarta.transaction.Transactional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private Mapper mapper;

    public List<GroupDTO> getAllGroups(Pageable pageable) {
        Page<Group> groupPage = groupRepository.findAll(pageable);
        List<GroupDTO> groupDTOs = groupPage.getContent().stream()
                .map(mapper::GroupToDTO)
                .collect(Collectors.toList());
        return groupDTOs;
    }

    public GroupDTO getGroupById(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return mapper.GroupToDTO(group);
    }

    @Transactional
    public GroupDTO createGroup(GroupDTO groupDTO) {
        Group group = mapper.DTOtoGroup(groupDTO);
        group = groupRepository.save(group);
        groupRepository.flush();
        User user = userService.getCurrentUser();
        GroupMemberDTO groupMemberDTO = new GroupMemberDTO();
        groupMemberDTO.setUserId(user.getId());
        groupMemberDTO.setGroupId(group.getId());
        groupMemberDTO.setIsAdmin(true);
        groupMemberDTO.setDisplayName(user.getUsername());
        groupMemberService.createGroupMember(groupMemberDTO);
        return mapper.GroupToDTO(group);
    }

    public GroupDTO updateGroup(UUID id, GroupDTO groupDTO) {
        Group existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        mapper.updateGroupFromDTO(groupDTO, existingGroup);
        existingGroup = groupRepository.save(existingGroup);
        return mapper.GroupToDTO(existingGroup);
    }

    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }
}