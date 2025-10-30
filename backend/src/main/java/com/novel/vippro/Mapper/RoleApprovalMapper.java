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
		var dto = new RoleApprovalDTO();
		dto.setUserId(roleApprovalRequest.getUser().getId());
		dto.setUsername(roleApprovalRequest.getUser().getUsername());
		dto.setRequestedRole(roleApprovalRequest.getRequestedRole().getName());
		dto.setStatus(roleApprovalRequest.getStatus());
		dto.setCreatedAt(roleApprovalRequest.getCreatedAt());
		dto.setUpdatedAt(roleApprovalRequest.getUpdatedAt());
		dto.setProcessedBy(roleApprovalRequest.getRejectionReason());
		dto.setRejectionReason(roleApprovalRequest.getRejectionReason());
		return dto;
	}

	public RoleApprovalRequest DTOtoRoleApprovalRequest(RoleApprovalDTO roleApprovalDTO) {
		return modelMapper.map(roleApprovalDTO, RoleApprovalRequest.class);
	}

	public void updateRoleApprovalRequestFromDTO(RoleApprovalDTO dto, RoleApprovalRequest roleApprovalRequest) {
		modelMapper.map(dto, roleApprovalRequest);
	}
}
