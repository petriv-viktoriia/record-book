package org.pnurecord.recordbook.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pnurecord.recordbook.reaction.Reaction;
import org.pnurecord.recordbook.record.Record;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Role role;

    @OneToMany(mappedBy = "author")
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Reaction> reactions = new ArrayList<>();
}
