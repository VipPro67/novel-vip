package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Models.Tag;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TagMapper {

    TagDTO TagtoDTO(Tag tag);

    Tag DTOtoTag(TagDTO tagDTO);

    List<TagDTO> TagListtoDTOList(List<Tag> tags);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTagFromDTO(TagDTO dto, @MappingTarget Tag tag);
}
