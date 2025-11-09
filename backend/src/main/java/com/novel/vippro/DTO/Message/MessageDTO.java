package com.novel.vippro.DTO.Message;

import java.util.UUID;

import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.DTO.base.BaseDTO;

import lombok.Data;

@Data
public class MessageDTO extends BaseDTO {
    private UUID id;
    private UserDTO sender;
    private UserDTO receiver;
    private GroupDTO group;
    private String content;
    private Boolean isRead;
}