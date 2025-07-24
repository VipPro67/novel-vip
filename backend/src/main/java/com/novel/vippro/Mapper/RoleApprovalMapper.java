package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.Models.RoleApprovalRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleApprovalMapper {
	@Autowired
	private ModelMapper modelMapper;

	public RoleApprovalDTO RoleApprovalRequestToDTO(RoleApprovalRequest roleApprovalRequest) {
		return modelMapper.map(roleApprovalRequest, RoleApprovalDTO.class);
	}

	public RoleApprovalRequest DTOtoRoleApprovalRequest(RoleApprovalDTO roleApprovalDTO) {
		return modelMapper.map(roleApprovalDTO, RoleApprovalRequest.class);
	}

	public void updateRoleApprovalRequestFromDTO(RoleApprovalDTO dto, RoleApprovalRequest roleApprovalRequest) {
		modelMapper.map(dto, roleApprovalRequest);
	}
}
