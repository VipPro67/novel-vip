package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.Models.RoleApprovalRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoleApprovalMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "requestedRole", source = "requestedRole.name")
    RoleApprovalDTO RoleApprovalRequestToDTO(RoleApprovalRequest roleApprovalRequest);

    RoleApprovalRequest DTOtoRoleApprovalRequest(RoleApprovalDTO roleApprovalDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleApprovalRequestFromDTO(RoleApprovalDTO dto, @MappingTarget RoleApprovalRequest roleApprovalRequest);
}
