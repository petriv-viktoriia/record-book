package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        boolean exists = categoryRepository.existsByName(categoryDto.getName());
        if (exists) {
            throw new DuplicateValueException("Category with %s already exists".formatted(categoryDto.getName()));
        } else {
            Category category = categoryMapper.toCategory(categoryDto);
            return categoryMapper.toCategoryDto(categoryRepository.save(category));
        }
    }

    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: %s".formatted(categoryId));
        } else {
            categoryRepository.deleteById(categoryId);
        }
    }

    public void deleteAllCategories() {
        if (categoryRepository.count() == 0) {
            throw new NotFoundException("No categories to delete. The table is already empty.");
        } else {
            categoryRepository.deleteAll();
        }
    }

    public List<CategoryDto> getAllCategories() {
        return categoryMapper.toCategoryDtoList(categoryRepository.findAll());
    }

    public CategoryDto findById(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: %s".formatted(categoryId)));
        return categoryMapper.toCategoryDto(category);
    }


    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: %s".formatted(categoryId)));

        boolean nameExists = categoryRepository.existsByName(categoryDto.getName());

        if (nameExists && !category.getName().equals(categoryDto.getName())) {
            throw new DuplicateValueException("Category with name '%s' already exists.".formatted(categoryDto.getName()));
        } else {
            category.setName(categoryDto.getName());
            return categoryMapper.toCategoryDto(categoryRepository.save(category));
        }
    }
}
