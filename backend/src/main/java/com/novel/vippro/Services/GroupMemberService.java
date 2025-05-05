package com.novel.vippro.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.GroupMember;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.GroupMemberRepository;

@Service
public class GroupMemberService {
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private Mapper mapper;

    public PageResponse<GroupMemberDTO> getAllGroupMembers(Pageable pageable) {
        Page<GroupMember> groupMemberPage = groupMemberRepository.findAll(pageable);
        PageResponse<GroupMemberDTO> groupMemberDTOs = new PageResponse<>();
        groupMemberDTOs.setContent(groupMemberPage.getContent().stream()
                .map(mapper::GroupMembertoDTO)
                .toList());
        return groupMemberDTOs;
    }

    public List<GroupMemberDTO> getMembersByGroupId(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(mapper::GroupMembertoDTO)
                .toList();
    }

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