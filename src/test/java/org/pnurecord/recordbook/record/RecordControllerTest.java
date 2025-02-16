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
import org.springframework.security.test.context.support.WithMockUser;
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getRecordById() throws Exception {
        mockMvc.perform(get("/records/" + savedRecord.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$.description").value(savedRecord.getDescription()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void gePendingRecordsByCategory() throws Exception {
        mockMvc.perform(get("/records/pending/categories/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].categoryId").value(savedCategory.getId()))
                .andExpect(jsonPath("$[0].id").value(savedRecord.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
        ;

        List<RecordDto> records = recordService.getPendingRecordsByCategory(savedCategory.getId());
        assertEquals(1, records.size());
        assertEquals(savedRecord.getTitle(), records.get(0).getTitle());
        assertEquals(savedCategory.getId(), records.get(0).getCategoryId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getApprovedRecordsByCategory() throws Exception {
        UserCreateDto userDto2 = new UserCreateDto();
        userDto2.setFirstName(UUID.randomUUID().toString());
        userDto2.setLastName(UUID.randomUUID().toString());
        userDto2.setEmail(UUID.randomUUID().toString());
        userDto2.setRole(Role.ADMIN);
        UserDto savedUser2 = userService.createUser(userDto2);

        RecordDto recordDto2 = new RecordDto();
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setAuthorId(savedUser2.getId());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());
        RecordDto savedRecord2 = recordService.createRecord(recordDto2);

        mockMvc.perform(get("/records/categories/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(savedRecord2.getTitle()))
                .andExpect(jsonPath("$[0].categoryId").value(savedCategory.getId()))
                .andExpect(jsonPath("$[0].id").value(savedRecord2.getId()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findAllPendingRecords() throws Exception {
        mockMvc.perform(get("/records/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findAllApprovedRecords() throws Exception {
        recordService.approveRecord(savedRecord.getId());

        mockMvc.perform(get("/records/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPendingRecordsByDate() throws Exception {
        mockMvc.perform(get("/records/pending/date/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getApprovedRecordsByDate() throws Exception {
        UserCreateDto userDto2 = new UserCreateDto();
        userDto2.setFirstName(UUID.randomUUID().toString());
        userDto2.setLastName(UUID.randomUUID().toString());
        userDto2.setEmail(UUID.randomUUID().toString());
        userDto2.setRole(Role.ADMIN);
        UserDto savedUser2 = userService.createUser(userDto2);

        RecordDto recordDto2 = new RecordDto();
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setAuthorId(savedUser2.getId());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());
        RecordDto savedRecord2 = recordService.createRecord(recordDto2);

        mockMvc.perform(get("/records/date/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedRecord2.getTitle()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getUserRecordsByStatus() throws Exception {
        mockMvc.perform(get("/records/users/" + savedUser.getId() + "?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorId").value(savedUser.getId()))
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void approveRecord() throws Exception {
        mockMvc.perform(put("/records/" + savedRecord.getId() + "/approve"))
                .andExpect(status().isOk());

        RecordDto approved = recordService.findById(savedRecord.getId());
        assertEquals(RecordStatus.APPROVED, approved.getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void rejectRecord() throws Exception {
        mockMvc.perform(put("/records/" + savedRecord.getId() + "/reject"))
                .andExpect(status().isOk());

        RecordDto rejected = recordService.findById(savedRecord.getId());
        assertEquals(RecordStatus.REJECTED, rejected.getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void searchApprovedOrPendingRecordsByTitle() throws Exception {
        recordDto.setTitle("Test" + UUID.randomUUID());
        savedRecord = recordService.createRecord(recordDto);
        UserCreateDto adminDto = new UserCreateDto();
        adminDto.setFirstName(UUID.randomUUID().toString());
        adminDto.setLastName(UUID.randomUUID().toString());
        adminDto.setEmail(UUID.randomUUID().toString());
        adminDto.setRole(Role.ADMIN);
        UserDto adminUser = userService.createUser(adminDto);

        RecordDto approvedRecord = new RecordDto();
        approvedRecord.setTitle("Test" + UUID.randomUUID());
        approvedRecord.setAuthorId(adminUser.getId());
        approvedRecord.setDescription(UUID.randomUUID().toString());
        approvedRecord.setCategoryId(savedCategory.getId());
        RecordDto savedApprovedRecord = recordService.createRecord(approvedRecord);

        mockMvc.perform(get("/records/search")
                        .param("title", "Test")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value(savedApprovedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        mockMvc.perform(get("/records/pending/search")
                        .param("title", "Test")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value(savedRecord.getTitle()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void searchRecordsByTitleTooShort() throws Exception {
        mockMvc.perform(get("/records/search?title=a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}