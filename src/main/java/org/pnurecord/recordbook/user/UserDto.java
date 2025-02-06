package org.pnurecord.recordbook.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
