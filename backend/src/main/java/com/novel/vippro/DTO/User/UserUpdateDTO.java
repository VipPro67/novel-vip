package com.novel.vippro.DTO.User;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserUpdateDTO {
    @Size(max = 120)
    private String fullName;

    private String avatar;
}