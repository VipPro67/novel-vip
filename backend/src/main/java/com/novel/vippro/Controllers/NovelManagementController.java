package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Services.CategoryService;
import com.novel.vippro.Services.GenreService;
import com.novel.vippro.Services.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/novels")
@Tag(name = "Novels", description = "Novel management APIs")
public class NovelManagementController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private GenreService genreService;

    @PostMapping("/categories")
    @Operation(summary = "Create category", description = "Create a new category")
    public ControllerResponse<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ControllerResponse.success("Category created successfully", createdCategory);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ControllerResponse<CategoryDTO> updateCategory(@PathVariable UUID id, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ControllerResponse.success("Category updated successfully", updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category", description = "Delete an existing category")
    public ControllerResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ControllerResponse.success("Category deleted successfully", null);
    }

    @PostMapping("/tags")
    @Operation(summary = "Create tag", description = "Create a new tag")
    public ControllerResponse<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        TagDTO createdTag = tagService.createTag(tagDTO);
        return ControllerResponse.success("Tag created successfully", createdTag);
    }

    @PutMapping("/tags/{id}")
    @Operation(summary = "Update tag", description = "Update an existing tag")
    public ControllerResponse<TagDTO> updateTag(@PathVariable UUID id, @RequestBody TagDTO tagDTO) {
        TagDTO updatedTag = tagService.updateTag(id, tagDTO);
        return ControllerResponse.success("Tag updated successfully", updatedTag);
    }

    @DeleteMapping("/tags/{id}")
    @Operation(summary = "Delete tag", description = "Delete an existing tag")
    public ControllerResponse<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ControllerResponse.success("Tag deleted successfully", null);
    }

    @PostMapping("/genres")
    @Operation(summary = "Create genre", description = "Create a new genre")
    public ControllerResponse<GenreDTO> createGenre(@RequestBody GenreDTO genreDTO) {
        GenreDTO createdGenre = genreService.createGenre(genreDTO);
        return ControllerResponse.success("Genre created successfully", createdGenre);
    }

    @PutMapping("/genres/{id}")
    @Operation(summary = "Update genre", description = "Update an existing genre")
    public ControllerResponse<GenreDTO> updateGenre(@PathVariable UUID id, @RequestBody GenreDTO genreDTO) {
        GenreDTO updatedGenre = genreService.updateGenre(id, genreDTO);
        return ControllerResponse.success("Genre updated successfully", updatedGenre);
    }

    @DeleteMapping("/genres/{id}")
    @Operation(summary = "Delete genre", description = "Delete an existing genre")
    public ControllerResponse<Void> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ControllerResponse.success("Genre deleted successfully", null);
    }
}