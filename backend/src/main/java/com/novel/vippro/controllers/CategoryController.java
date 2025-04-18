package com.novel.vippro.controllers;

import com.novel.vippro.models.Category;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.services.CategoryService;
import com.novel.vippro.dto.CategoryDTO;
import com.novel.vippro.dto.NovelDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Novel category management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

        @Autowired
        private CategoryService categoryService;

        @Operation(summary = "Get all categories", description = "Get a paginated list of all categories")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
        })
        @GetMapping
        public ControllerResponse<PageResponse<CategoryDTO>> getAllCategories(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<CategoryDTO> categories = categoryService.getAllCategories(pageable);
                return ControllerResponse.success("Categories retrieved successfully", categories);
        }

        @Operation(summary = "Get category by ID", description = "Get detailed information about a specific category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category found"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        @GetMapping("/{id}")
        public ControllerResponse<Category> getCategoryById(
                        @Parameter(description = "Category ID", required = true) @PathVariable UUID id) {
                Category category = categoryService.getCategoryById(id);
                return ControllerResponse.success("Category retrieved successfully", category);
        }

        @Operation(summary = "Create category", description = "Create a new novel category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid category data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<Category> createCategory(
                        @Parameter(description = "Category details", required = true) @Valid @RequestBody Category category) {
                Category createdCategory = categoryService.createCategory(category);
                return ControllerResponse.success("Category created successfully", createdCategory);
        }

        @Operation(summary = "Update category", description = "Update an existing category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid category data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<Category> updateCategory(
                        @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Updated category details", required = true) @Valid @RequestBody Category categoryDetails) {
                Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
                return ControllerResponse.success("Category updated successfully", updatedCategory);
        }

        @Operation(summary = "Delete category", description = "Delete an existing category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<Void> deleteCategory(
                        @Parameter(description = "Category ID", required = true) @PathVariable UUID id) {
                categoryService.deleteCategory(id);
                return ControllerResponse.success("Category deleted successfully", null);
        }

        @Operation(summary = "Get novels by category", description = "Get all novels in a specific category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Novels retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        @GetMapping("/{category}/novels")
        public ControllerResponse<PageResponse<NovelDTO>> getNovelsByCategory(
                        @Parameter(description = "Category name", required = true) @PathVariable String category,
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<NovelDTO> novels = categoryService.getNovelsByCategory(category, pageable);
                return ControllerResponse.success("Novels retrieved successfully", novels);
        }
}