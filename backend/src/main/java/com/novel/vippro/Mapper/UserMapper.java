package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO UsertoUserDTO(User user);

    User DTOtoUser(UserDTO userDTO);

    List<UserDTO> UserListtoDTOList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UserDTO dto, @MappingTarget User user);
}
