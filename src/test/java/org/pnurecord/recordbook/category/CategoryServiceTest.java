package org.pnurecord.recordbook.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class CategoryServiceTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    void testCreateReadDelete(){
        CategoryDTO category = new CategoryDTO();
        category.setName("Test Category");
        category.setRecords(new ArrayList<>());
        categoryService.createCategory(category);

        List<CategoryDTO> categories = categoryService.getAllCategories();
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo(category.getName());

        categoryService.deleteAllCategories();
        assertThat(categoryService.getAllCategories()).isEmpty();
    }
}





