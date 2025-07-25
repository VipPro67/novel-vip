package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.Models.Category;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

	@Autowired
	private ModelMapper modelMapper;

	public CategoryDTO CategorytoDTO(Category category) {
		return modelMapper.map(category, CategoryDTO.class);
	}

	public Category DTOtoCategory(CategoryDTO categoryDTO) {
		return modelMapper.map(categoryDTO, Category.class);
	}

	public List<CategoryDTO> CategoryListtoDTOList(List<Category> categories) {
		return categories.stream()
				.map(this::CategorytoDTO)
				.collect(Collectors.toList());
	}

	public void updateCategoryFromDTO(CategoryDTO dto, Category category) {
		modelMapper.map(dto, category);
	}
}