package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
	@Autowired
	private ModelMapper modelMapper;

	public UserDTO UsertoUserDTO(User user) {
		
		return modelMapper.map(user, UserDTO.class);
	}

	public User DTOtoUser(UserDTO userDTO) {
		return modelMapper.map(userDTO, User.class);
	}

	public List<UserDTO> UserListtoDTOList(List<User> users) {
		return users.stream()
				.map(this::UsertoUserDTO)
				.collect(Collectors.toList());
	}

	public void updateUserFromDTO(UserDTO dto, User user) {
		modelMapper.map(dto, user);
	}
}
