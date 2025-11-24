package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Models.GroupMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface GroupMemberMapper {

    GroupMemberDTO GroupMembertoDTO(GroupMember groupMember);

    GroupMember DTOtoGroupMember(GroupMemberDTO groupMemberDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateGroupMemberFromDTO(GroupMemberDTO dto, @MappingTarget GroupMember groupMember);
}
