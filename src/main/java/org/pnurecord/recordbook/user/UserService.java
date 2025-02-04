package org.pnurecord.recordbook.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public Iterable<User> findAll() {
        return this.userRepository.findAll();
    }


}
