package com.novel.vippro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(max = 120)
    private String fullName;

    @Size(max = 50)
    @Email
    private String email;

    @Size(max = 120)
    private String currentPassword;

    @Size(max = 120)
    private String newPassword;
}