package com.novel.vippro.DTO.Video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVideoDTO {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    @NotBlank
    @Size(max = 1024)
    private String videoUrl;
}
