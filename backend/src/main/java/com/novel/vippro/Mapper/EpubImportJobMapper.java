package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Epub.EpubImportJobDTO;
import com.novel.vippro.Models.EpubImportJob;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EpubImportJobMapper {

    @Autowired
    private ModelMapper modelMapper;

    public EpubImportJobDTO toDTO(EpubImportJob job) {
        if (job == null) {
            return null;
        }
        return modelMapper.map(job, EpubImportJobDTO.class);
    }
}
