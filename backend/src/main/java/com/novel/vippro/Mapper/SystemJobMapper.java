package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.System.SystemJobDTO;
import com.novel.vippro.Models.SystemJob;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemJobMapper {

    SystemJobDTO toDTO(SystemJob job);
}
