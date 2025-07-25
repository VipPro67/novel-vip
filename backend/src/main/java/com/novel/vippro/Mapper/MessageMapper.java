package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Models.Message;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
	@Autowired
	private ModelMapper modelMapper;

	public MessageDTO MessagetoDTO(Message message) {
		return modelMapper.map(message, MessageDTO.class);
	}

	public Message DTOtoMessage(MessageDTO messageDTO) {
		return modelMapper.map(messageDTO, Message.class);
	}

	public Message CreateDTOtoMessage(CreateMessageDTO messageDTO) {
		return modelMapper.map(messageDTO, Message.class);
	}

	public void updateMessageFromDTO(MessageDTO dto, Message message) {
		modelMapper.map(dto, message);
	}
}
