package com.novel.vippro.DTO.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRejectDTO {
    @NotBlank
    private String reason;

}