package com.novel.vippro.DTO.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.novel.vippro.Models.Role;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private Set<Role> roles = new HashSet<>();
    private Instant createdAt;
    private Instant updatedAt;
    // Constructor for basic user info
    public UserDTO(UUID id, String username, String email, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

    // Default constructor
    public UserDTO() {
    }
}