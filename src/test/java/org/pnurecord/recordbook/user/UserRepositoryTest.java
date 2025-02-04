package org.pnurecord.recordbook.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateReadDelete() {
        User user = new User(1L, "test@example.com", "dude", "dude");
        userRepository.save(user);

        Iterable<User> users = userRepository.findAll();
        assertThat(users).extracting(User::getEmail).containsOnly("test@example.com");

        userRepository.deleteAll();
        assertThat(userRepository.findAll()).isEmpty();
    }
}