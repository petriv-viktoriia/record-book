package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = convertToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public void deleteAllCategories() {
        categoryRepository.deleteAll();
    }

    public Optional<CategoryDto> findById(long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO);
    }

    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        category.get().setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category.get());
        return convertToDTO(updatedCategory);
    }


    private CategoryDto convertToDTO(Category category) {
        if (category == null) {
            return null;
        }
        CategoryDto categoryDTO = new CategoryDto();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        return categoryDTO;
    }


    private Category convertToEntity(CategoryDto categoryDTO) {
        if (categoryDTO == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setName(categoryDTO.getName());
        return category;
    }


}
