package com.novel.vippro.DTO.User;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserSearchDTO {
    private String username;
    private String email;
    private String role;
    private Boolean active;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "username";
    private String sortDirection = "asc";
}