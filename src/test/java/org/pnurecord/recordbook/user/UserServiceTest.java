package org.pnurecord.recordbook.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest extends AbstractTestContainerBaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto savedUserDto;
    private UserCreateDto userCreateDto;

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        userCreateDto = new UserCreateDto();
        userCreateDto.setFirstName(UUID.randomUUID().toString());
        userCreateDto.setLastName(UUID.randomUUID().toString());
        userCreateDto.setEmail(UUID.randomUUID() + "@test.com");
        userCreateDto.setRole(Role.STUDENT);
    }

    @Test
    void testCreateUser() {
        savedUserDto = userService.createUser(userCreateDto);

        assertNotNull(savedUserDto.getId());
        assertEquals(userCreateDto.getFirstName(), savedUserDto.getFirstName());
        assertEquals(userCreateDto.getLastName(), savedUserDto.getLastName());
        assertEquals(userCreateDto.getEmail(), savedUserDto.getEmail());
        assertEquals(userCreateDto.getRole(), savedUserDto.getRole());
    }

    @Test
    void testUpdateUser() {
        savedUserDto = userService.createUser(userCreateDto);

        UserUpdateDto updateUserDto = new UserUpdateDto();
        updateUserDto.setFirstName("UpdatedFirstName");
        updateUserDto.setLastName("UpdatedLastName");

        userService.updateUser(savedUserDto.getId(), updateUserDto);

        UserDto updatedUser = userService.getUserById(savedUserDto.getId());

        assertNotNull(updatedUser);
        assertEquals("UpdatedFirstName", updatedUser.getFirstName());
        assertEquals("UpdatedLastName", updatedUser.getLastName());
        assertEquals(savedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void testGetUserById() {
        savedUserDto = userService.createUser(userCreateDto);
        UserDto foundUser = userService.getUserById(savedUserDto.getId());

        assertNotNull(foundUser);
        assertEquals(savedUserDto.getId(), foundUser.getId());
        assertEquals(savedUserDto.getFirstName(), foundUser.getFirstName());
        assertEquals(savedUserDto.getLastName(), foundUser.getLastName());
        assertEquals(savedUserDto.getEmail(), foundUser.getEmail());
    }

    @Test
    void testDeleteUser() {
        savedUserDto = userService.createUser(userCreateDto);
        userService.deleteUser(savedUserDto.getId());

        assertFalse(userRepository.existsById(savedUserDto.getId()));
    }

    @Test
    void testGetUsersByRole() {
        savedUserDto = userService.createUser(userCreateDto);
        List<UserDto> usersWithRole = userService.getUsersByRole(Role.STUDENT);

        assertFalse(usersWithRole.isEmpty());
        assertEquals(Role.STUDENT, usersWithRole.get(0).getRole());
    }

    @Test
    void testSetUserRole() {
        savedUserDto = userService.createUser(userCreateDto);

        UserDto updatedUser = userService.setUserRole(savedUserDto.getId(), Role.ADMIN);
        assertEquals(Role.ADMIN, updatedUser.getRole());

        UserDto foundUser = userService.getUserById(savedUserDto.getId());
        assertEquals(Role.ADMIN, foundUser.getRole());
    }

    @Test
    void testGetAllUsers() {
        int userCount = 5;
        for (int i = 0; i < userCount; i++) {
            UserCreateDto userCreateDto = new UserCreateDto();
            userCreateDto.setFirstName("First" + i);
            userCreateDto.setLastName("Last" + i);
            userCreateDto.setEmail(UUID.randomUUID() + "@test.com");
            userCreateDto.setRole(Role.STUDENT);
            userService.createUser(userCreateDto);
        }

        List<UserDto> allUsersDto = userService.getUsers();
        assertEquals(userCount, allUsersDto.size());
    }

}
