package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable Long categoryId) {
        return categoryService.findById(categoryId);
    }

    @PostMapping
    public void createCategory(@RequestBody CategoryDto category) {
        categoryService.createCategory(category);
    }

    @PutMapping("/{categoryId}")
    public CategoryDto updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDto category) {
        return categoryService.updateCategory(categoryId, category);
    }

    @DeleteMapping("/{categoryId}")
    public void deleteCategoryById(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @DeleteMapping
    public void deleteAllCategories() {
        categoryService.deleteAllCategories();
    }
}
