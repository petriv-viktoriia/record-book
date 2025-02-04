package org.pnurecord.recordbook.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }


}
