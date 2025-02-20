package org.pnurecord.recordbook.category;

import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryServiceTest extends AbstractTestContainerBaseTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;

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
        if (categoryRepository.count() > 0) {
            categoryService.deleteAllCategories();
        }

        int numberOfCategoriesToCreate = 10;

        for (int i = 0; i < numberOfCategoriesToCreate; i++) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(UUID.randomUUID().toString());

            CategoryDto savedCategory = categoryService.createCategory(categoryDto);
            assertNotNull(savedCategory.getId(), "Saved category should have an ID");
        }

        List<CategoryDto> categories = categoryService.getAllCategories();
        assertThat(categories).hasSize(numberOfCategoriesToCreate);
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

        CategoryDto foundCategory2 = categoryService.findById(savedCategory2.getId());
        assertNotNull(foundCategory2, "Category should still exist after deletion of another category");
        assertEquals(foundCategory2.getName(), savedCategory2.getName());
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
        int numberOfCategoriesToCreate = 10;
        List<CategoryDto> savedCategories = new ArrayList<>();

        for (int i = 0; i < numberOfCategoriesToCreate; i++) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(UUID.randomUUID().toString());

            CategoryDto savedCategory = categoryService.createCategory(categoryDto);
            assertNotNull(savedCategory.getId(), "Saved category should have an ID");
            savedCategories.add(savedCategory);
        }

        categoryService.deleteAllCategories();

        List<CategoryDto> categoriesAfterDelete = categoryService.getAllCategories();
        assertTrue(categoriesAfterDelete.isEmpty(), "All categories should be deleted");

        for (CategoryDto category : savedCategories) {
            assertThrows(NotFoundException.class, () -> categoryService.findById(category.getId()),
                    "Category should not be found after deletion");
        }
    }

}
