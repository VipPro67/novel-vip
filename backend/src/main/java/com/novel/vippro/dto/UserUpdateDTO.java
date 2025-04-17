package com.novel.vippro.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(max = 120)
    private String fullName;

    private String avatar;
}