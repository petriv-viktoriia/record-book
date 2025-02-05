package org.pnurecord.recordbook.category;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.record.RecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final RecordService recordService;

    public CategoryDTO createCategory(CategoryDTO categoryDto) {
        Category category = convertToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public void deleteAllCategories() {
        categoryRepository.deleteAll();
    }

    public Optional<CategoryDTO> findById(long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO);
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDto) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        category.get().setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category.get());
        return convertToDTO(updatedCategory);
    }


    private CategoryDTO convertToDTO(Category category) {
        if (category == null) {
            return null;
        }
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        return categoryDTO;
    }


    private Category convertToEntity(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setName(categoryDTO.getName());
        return category;
    }


}
