package org.pnurecord.recordbook.record;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.UserCreateDto;
import org.pnurecord.recordbook.user.UserDto;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecordControllerTest extends AbstractTestContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecordService recordService;

    private RecordDto savedRecord;
    private UserDto savedUser;
    private CategoryDto savedCategory;
    private RecordDto recordDto;

    @AfterEach
    void tearDown() {
        recordService.deleteAllRecords();
    }

    @BeforeEach
    void setUp() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID().toString());
        userDto.setRole(Role.STUDENT);
        savedUser = userService.createUser(userDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);

        recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(savedUser.getId());
        recordDto.setDescription(UUID.randomUUID().toString());
        recordDto.setCategoryId(savedCategory.getId());
        savedRecord = recordService.createRecord(recordDto);
    }

    @Test
    void findAllRecords() throws Exception {
        mockMvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].description").value(savedRecord.getDescription()));

        List<RecordDto> records = recordService.findAllRecords();
        assertEquals(1, records.size());
        assertEquals(savedRecord.getTitle(), records.get(0).getTitle());
    }

    @Test
    void getRecordById() throws Exception {
        mockMvc.perform(get("/records/" + savedRecord.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$.description").value(savedRecord.getDescription()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createRecord() throws Exception {
        RecordDto newRecordDto = new RecordDto();
        newRecordDto.setTitle(UUID.randomUUID().toString());
        newRecordDto.setDescription(UUID.randomUUID().toString());
        newRecordDto.setCategoryId(savedCategory.getId());
        newRecordDto.setAuthorId(savedUser.getId());

        String content = String.format("""
            {
                "title": "%s",
                "description": "%s",
                "categoryId": %d,
                "authorId": %d
            }""", newRecordDto.getTitle(), newRecordDto.getDescription(),
                newRecordDto.getCategoryId(), newRecordDto.getAuthorId());

        mockMvc.perform(post("/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(newRecordDto.getTitle()))
                .andExpect(jsonPath("$.description").value(newRecordDto.getDescription()));

        List<RecordDto> records = recordService.findAllRecords();
        assertEquals(2, records.size());
    }

    @Test
    void updateRecord() throws Exception {
        String updatedTitle = UUID.randomUUID().toString();
        String updatedDescription = UUID.randomUUID().toString();

        String content = String.format("""
            {
                "title": "%s",
                "description": "%s",
                "categoryId": %d,
                "authorId": %d
            }""", updatedTitle, updatedDescription,
                savedCategory.getId(), savedUser.getId());

        mockMvc.perform(put("/records/" + savedRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.description").value(updatedDescription));

        RecordDto updated = recordService.findById(savedRecord.getId());
        assertEquals(updatedTitle, updated.getTitle());
        assertEquals(updatedDescription, updated.getDescription());
    }

    @Test
    void deleteRecordById() throws Exception {
        int initialCount = recordService.findAllRecords().size();
        assertEquals(1, initialCount);

        mockMvc.perform(delete("/records/" + savedRecord.getId()))
                .andExpect(status().isOk());

        List<RecordDto> records = recordService.findAllRecords();
        assertTrue(records.isEmpty());
        assertThrows(NotFoundException.class, () -> recordService.findById(savedRecord.getId()));
    }

    @Test
    void getRecordsByCategory() throws Exception {
        mockMvc.perform(get("/records/categories/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].categoryId").value(savedCategory.getId()))
                .andExpect(jsonPath("$[0].id").value(savedRecord.getId()));

        List<RecordDto> records = recordService.getRecordsByCategory(savedCategory.getId());
        assertEquals(1, records.size());
        assertEquals(savedRecord.getTitle(), records.get(0).getTitle());
        assertEquals(savedCategory.getId(), records.get(0).getCategoryId());
    }

    @Test
    void getRecordsByAuthor() throws Exception {
        mockMvc.perform(get("/records/authors/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].authorId").value(savedUser.getId()))
                .andExpect(jsonPath("$[0].id").value(savedRecord.getId()));

        List<RecordDto> records = recordService.getRecordsByAuthor(savedUser.getId());
        assertEquals(1, records.size());
        assertEquals(savedRecord.getTitle(), records.get(0).getTitle());
        assertEquals(savedUser.getId(), records.get(0).getAuthorId());
    }

    @Test
    void findAllPendingRecords() throws Exception {
        mockMvc.perform(get("/records/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void findAllApprovedRecords() throws Exception {
        recordService.approveRecord(savedRecord.getId());

        mockMvc.perform(get("/records/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void getRecordsByDate() throws Exception {
        mockMvc.perform(get("/records/date/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()));
    }

    @Test
    void getUserRecordsByStatus() throws Exception {
        mockMvc.perform(get("/records/users/" + savedUser.getId() + "?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorId").value(savedUser.getId()))
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approveRecord() throws Exception {
        mockMvc.perform(put("/records/" + savedRecord.getId() + "/approve"))
                .andExpect(status().isOk());

        RecordDto approved = recordService.findById(savedRecord.getId());
        assertEquals(RecordStatus.APPROVED, approved.getStatus());
    }

    @Test
    void rejectRecord() throws Exception {
        mockMvc.perform(put("/records/" + savedRecord.getId() + "/reject"))
                .andExpect(status().isOk());

        RecordDto rejected = recordService.findById(savedRecord.getId());
        assertEquals(RecordStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void searchRecordsByTitle() throws Exception {
        RecordDto recordDto2 = new RecordDto();
        recordDto2.setTitle("Test" + UUID.randomUUID());
        recordDto2.setAuthorId(savedUser.getId());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());
        recordService.createRecord(recordDto2);

        RecordDto recordDto3 = new RecordDto();
        recordDto3.setTitle("Test" + UUID.randomUUID());
        recordDto3.setAuthorId(savedUser.getId());
        recordDto3.setDescription(UUID.randomUUID().toString());
        recordDto3.setCategoryId(savedCategory.getId());
        recordService.createRecord(recordDto3);

        mockMvc.perform(get("/records/search?title=Test&limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value(recordDto2.getTitle()))
                .andExpect(jsonPath("$[1].title").value(recordDto3.getTitle()));
    }

    @Test
    void searchRecordsByTitleTooShort() throws Exception {
        mockMvc.perform(get("/records/search?title=a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}