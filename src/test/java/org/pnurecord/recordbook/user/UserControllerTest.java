package org.pnurecord.recordbook.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends AbstractTestContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        userCreateDto = new UserCreateDto();
        userCreateDto.setEmail(UUID.randomUUID() + "@example.com");
        userCreateDto.setFirstName(UUID.randomUUID().toString());
        userCreateDto.setLastName(UUID.randomUUID().toString());
        userCreateDto.setRole(Role.STUDENT);

        userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName(UUID.randomUUID().toString());
        userUpdateDto.setLastName(UUID.randomUUID().toString());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);
        List<UserDto> expectedUsers = List.of(createdUser);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUser() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);

        mockMvc.perform(get("/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(createdUser)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser() throws Exception {
        mockMvc.perform(post("/registry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);

        mockMvc.perform(delete("/users/" + createdUser.getId()))
                .andExpect(status().isOk());

        assertFalse(userRepository.findById(createdUser.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);

        mockMvc.perform(put("/users/" + createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUsersByRole() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);
        List<UserDto> expectedUsers = List.of(createdUser);

        mockMvc.perform(get("/role/" + createdUser.getRole()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSetUserRole() throws Exception {
        UserDto createdUser = userService.createUser(userCreateDto);

        mockMvc.perform(put("/users/" + createdUser.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Role.ADMIN)))
                .andExpect(status().isOk());
    }
}
