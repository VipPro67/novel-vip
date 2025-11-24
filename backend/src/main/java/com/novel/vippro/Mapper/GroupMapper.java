package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Group.CreateGroupDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.Group.UpdateGroupDTO;
import com.novel.vippro.Models.Group;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface GroupMapper {

    GroupDTO GroupToDTO(Group group);

    Group DTOtoGroup(GroupDTO groupDTO);

    Group CreateDTOtoGroup(CreateGroupDTO groupDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateGroupFromDTO(UpdateGroupDTO dto, @MappingTarget Group group);
}
