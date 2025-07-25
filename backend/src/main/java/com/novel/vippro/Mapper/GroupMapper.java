package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Group.CreateGroupDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.Group.UpdateGroupDTO;
import com.novel.vippro.Models.Group;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {
	@Autowired
	private ModelMapper modelMapper;

	public GroupDTO GroupToDTO(Group group) {
		return modelMapper.map(group, GroupDTO.class);
	}

	public Group DTOtoGroup(GroupDTO groupDTO) {
		return modelMapper.map(groupDTO, Group.class);
	}

	public Group CreateDTOtoGroup(CreateGroupDTO groupDTO) {
		return modelMapper.map(groupDTO, Group.class);
	}

	public void updateGroupFromDTO(UpdateGroupDTO dto, Group group) {
		modelMapper.map(dto, group);
	}
}
