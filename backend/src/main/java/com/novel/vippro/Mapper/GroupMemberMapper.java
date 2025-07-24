package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Models.GroupMember;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberMapper {
	@Autowired
	private ModelMapper modelMapper;

	public GroupMemberDTO GroupMembertoDTO(GroupMember groupMember) {
		return modelMapper.map(groupMember, GroupMemberDTO.class);
	}

	public GroupMember DTOtoGroupMember(GroupMemberDTO groupMemberDTO) {
		return modelMapper.map(groupMemberDTO, GroupMember.class);
	}

	public void updateGroupMemberFromDTO(GroupMemberDTO dto, GroupMember groupMember) {
		modelMapper.map(dto, groupMember);
	}
}
