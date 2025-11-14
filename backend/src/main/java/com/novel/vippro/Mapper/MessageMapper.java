package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.Models.Message;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageDTO MessagetoDTO(Message message);

    Message DTOtoMessage(MessageDTO messageDTO);

    Message CreateDTOtoMessage(CreateMessageDTO messageDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMessageFromDTO(MessageDTO dto, @MappingTarget Message message);
}
