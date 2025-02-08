package org.pnurecord.recordbook.category;

import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryServiceTest extends AbstractTestContainerBaseTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    void testCreate() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        CategoryDto savedCategory = categoryService.createCategory(categoryDto);
        assertNotNull(savedCategory.getId(), "Saved category should have an ID");
        assertThat(savedCategory.getName()).isEqualTo(categoryDto.getName());
    }

    @Test
    void testFindAllCategories() {
        categoryService.deleteAllCategories();
        CategoryDto category1 = new CategoryDto();
        category1.setName(UUID.randomUUID().toString());

        CategoryDto savedCategory = categoryService.createCategory(category1);
        assertNotNull(savedCategory.getId(), "Saved category should have an ID");

        List<CategoryDto> categories = categoryService.getAllCategories();
        assertThat(categories).hasSize(1);
    }

    @Test
    void testGetById() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());

        CategoryDto savedCategory = categoryService.createCategory(categoryDto);

        assertNotNull(savedCategory.getId(), "Saved category should have an ID");
        assertThat(savedCategory.getName()).isEqualTo(categoryDto.getName());

        CategoryDto foundById = categoryService.findById(savedCategory.getId());
        assertNotNull(foundById, "Category should be found by ID");
        assertThat(foundById.getName()).isEqualTo(savedCategory.getName());
        assertThat(foundById.getId()).isEqualTo(savedCategory.getId());
    }

    @Test
    void testDeleteById() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());

        CategoryDto categoryDto2 = new CategoryDto();
        categoryDto2.setName(UUID.randomUUID().toString());

        CategoryDto savedCategory = categoryService.createCategory(categoryDto);
        assertNotNull(savedCategory.getId(), "Saved category should have an ID");

        CategoryDto savedCategory2 = categoryService.createCategory(categoryDto2);
        assertNotNull(savedCategory2.getId(), "Saved category should have an ID");

        categoryService.deleteCategory(savedCategory.getId());

        assertThrows(NotFoundException.class, () -> categoryService.findById(savedCategory.getId()));
    }

    @Test
    void testUpdate() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        CategoryDto savedCategory = categoryService.createCategory(categoryDto);

        assertNotNull(savedCategory.getId(), "Saved category should have an ID");

        CategoryDto updatedCategory = new CategoryDto();
        updatedCategory.setName(UUID.randomUUID().toString());

        categoryService.updateCategory(savedCategory.getId(), updatedCategory);

        CategoryDto retrievedCategory = categoryService.findById(savedCategory.getId());

        assertNotNull(retrievedCategory, "Retrieved category should exist after update");
        assertThat(retrievedCategory.getId()).isEqualTo(savedCategory.getId());
        assertThat(retrievedCategory.getName()).isEqualTo(updatedCategory.getName(), "Category name should be updated");
    }


    @Test
    void testDeleteAllCategories() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        CategoryDto savedCategory = categoryService.createCategory(categoryDto);
        assertNotNull(savedCategory.getId(), "Saved category should have an ID");
        categoryService.deleteAllCategories();
        assertTrue(categoryService.getAllCategories().isEmpty(), "Table should be empty after delete");
    }

}
