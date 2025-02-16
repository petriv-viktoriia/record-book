package org.pnurecord.recordbook.reaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.record.RecordDto;
import org.pnurecord.recordbook.record.RecordService;
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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReactionControllerTest extends AbstractTestContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordService recordService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ReactionDto testReaction;
    private UserDto savedUser;
    private CategoryDto savedCategory;
    private RecordDto savedRecord;

    @Autowired
    private ReactionRepository reactionRepository;

    @AfterEach
    public void tearDown() {
        reactionRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID() + "@test.com");
        userDto.setRole(Role.STUDENT);
        savedUser = userService.createUser(userDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);

        RecordDto recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(savedUser.getId());
        recordDto.setDescription("Test record description");
        recordDto.setCategoryId(savedCategory.getId());
        savedRecord = recordService.createRecord(recordDto);

        testReaction = new ReactionDto();
        testReaction.setRecordId(savedRecord.getId());
        testReaction.setUserId(savedUser.getId());
        testReaction.setLiked(true);
        testReaction = reactionService.addReaction(testReaction);
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetAllReactions() throws Exception {
        mockMvc.perform(get("/reactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetReactionById() throws Exception {
        mockMvc.perform(get("/reactions/{id}", testReaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(testReaction.isLiked()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldNotCreateDuplicateReaction() throws Exception {
        UserCreateDto userDto2 = new UserCreateDto();
        userDto2.setFirstName(UUID.randomUUID().toString());
        userDto2.setLastName(UUID.randomUUID().toString());
        userDto2.setEmail(UUID.randomUUID() + "@test.com");
        userDto2.setRole(Role.STUDENT);

        UserDto savedUser2 = userService.createUser(userDto2);

        ReactionDto newReaction = new ReactionDto();
        newReaction.setRecordId(savedRecord.getId());
        newReaction.setUserId(savedUser2.getId());
        newReaction.setLiked(false);

        mockMvc.perform(post("/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReaction)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReaction)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateReaction() throws Exception {
        ReactionUpdateDto updateDto = new ReactionUpdateDto();
        updateDto.setLiked(false);

        mockMvc.perform(put("/reactions/{id}", testReaction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteReactionById() throws Exception {
        mockMvc.perform(delete("/reactions/{id}", testReaction.getId()))
                .andExpect(status().isOk());

        assertThrows(NotFoundException.class, () -> reactionService.getReactionById(testReaction.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetReactionCount() throws Exception {
        mockMvc.perform(get("/reactions/count").param("record-id", savedRecord.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.dislikes").value(0));
    }
}
