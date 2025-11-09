package com.novel.vippro.DTO.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.novel.vippro.DTO.base.BaseDTO;
import com.novel.vippro.Models.Role;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO extends BaseDTO {
    private String username;
    private String email;
    private String fullName;
    private List<Role> roles = new ArrayList<>();
    public UserDTO(UUID id, String username, String email, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }
    public UserDTO() {
    }
}