package org.pnurecord.recordbook.category;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest extends AbstractTestContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CategoryDto testCategory;

    @AfterEach
    public void tearDown() {
        if (!categoryService.getAllCategories().isEmpty()) {
            categoryService.deleteAllCategories();
        }
    }

    @BeforeEach
    void setup() {
        testCategory = new CategoryDto();
        testCategory.setName(UUID.randomUUID().toString());
        testCategory = categoryService.createCategory(testCategory);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllCategories() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetCategoryById() throws Exception {
        mockMvc.perform(get("/categories/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testCategory.getName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateCategory() throws Exception {
        CategoryDto newCategory = new CategoryDto();
        newCategory.setName("New Test Category");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Test Category"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateCategory() throws Exception {
        CategoryDto updatedCategory = new CategoryDto();
        updatedCategory.setName("Updated Name");

        mockMvc.perform(put("/categories/{categoryId}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteCategoryById() throws Exception {
        mockMvc.perform(delete("/categories/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk());

        assertThrows(NotFoundException.class, () -> categoryService.findById(testCategory.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteAllCategories() throws Exception {
        mockMvc.perform(delete("/categories"))
                .andExpect(status().isOk());

        assertTrue(categoryService.getAllCategories().isEmpty());
    }
}
