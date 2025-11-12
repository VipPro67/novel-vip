package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.System.SystemJobDTO;
import com.novel.vippro.Models.SystemJob;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemJobMapper {

    @Autowired
    private ModelMapper modelMapper;

    public SystemJobDTO toDTO(SystemJob job) {
        if (job == null) {
            return null;
        }
        return modelMapper.map(job, SystemJobDTO.class);
    }
}
