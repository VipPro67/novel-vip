package com.novel.vippro.Config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.TypeMap;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.DTO.Novel.NovelDTO;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(AccessLevel.PRIVATE)
            .setSkipNullEnabled(true);

        TypeMap<Novel, NovelDTO> typeMap = modelMapper.createTypeMap(Novel.class, NovelDTO.class);
        typeMap.setPropertyCondition(context -> context.getMapping() != null);

        return modelMapper;
    }
}
