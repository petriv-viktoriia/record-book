package org.pnurecord.recordbook.user;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;
}
