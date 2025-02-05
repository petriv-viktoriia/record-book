package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public void createCategory(CategoryDto categoryDto) {
        boolean exists = categoryRepository.existsByName(categoryDto.getName());
        if (exists) {
            throw new IllegalArgumentException("Category with %s already exists".formatted(categoryDto.getName()));
        } else {
            Category category = categoryMapper.toCategory(categoryDto);
            categoryRepository.save(category);
        }
    }

    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found with id: " + categoryId);
        } else {
            categoryRepository.deleteById(categoryId);
        }
    }

    public void deleteAllCategories() {
        if (categoryRepository.count() == 0) {
            throw new IllegalStateException("No categories to delete. The table is already empty.");
        } else {
            categoryRepository.deleteAll();
        }
    }

    public List<CategoryDto> getAllCategories() {
        return categoryMapper.toCategoryDtoList(categoryRepository.findAll());
    }

    public CategoryDto findById(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        return categoryMapper.toCategoryDto(category);
    }


    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        boolean nameExists = categoryRepository.existsByName(categoryDto.getName());
        if (nameExists && !category.getName().equals(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists.");
        }
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(updatedCategory);
    }

}
