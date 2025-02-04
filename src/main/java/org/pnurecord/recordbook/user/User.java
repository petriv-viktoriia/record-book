package org.pnurecord.recordbook.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Role role;
}
