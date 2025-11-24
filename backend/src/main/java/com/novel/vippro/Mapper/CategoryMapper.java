package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.Models.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryDTO CategorytoDTO(Category category);

    Category DTOtoCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> CategoryListtoDTOList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryFromDTO(CategoryDTO dto, @MappingTarget Category category);
}
