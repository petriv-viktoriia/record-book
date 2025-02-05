package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{categoryId}")
    public Optional<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        return categoryService.findById(categoryId);
    }

    @PostMapping
    public CategoryDTO createCategory(@RequestBody CategoryDTO category) {
        return categoryService.createCategory(category);
    }

    @PutMapping("{categoryId}")
    public CategoryDTO updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO category) {
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
