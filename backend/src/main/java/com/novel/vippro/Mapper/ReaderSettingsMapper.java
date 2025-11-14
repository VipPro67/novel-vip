package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.Models.ReaderSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReaderSettingsMapper {

    ReaderSettingsDTO ReaderSettingsToReaderSettingsDTO(ReaderSettings readerSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReaderSettingsFromDTO(ReaderSettingsUpdateDTO dto, @MappingTarget ReaderSettings settings);
}
