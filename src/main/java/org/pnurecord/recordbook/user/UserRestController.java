package org.pnurecord.recordbook.user;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserRestController {

    private final UserService userService;

    public Iterable<User> findAll() {
        return this.userService.findAll();
    }

}
