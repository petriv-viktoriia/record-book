package org.pnurecord.recordbook.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    @Query("SELECT CONCAT(u.firstName, ' ', u.lastName) FROM User u WHERE u.id = :id")
    String findUserNameById(@Param("id") Long id);

    Optional<User> findByEmail(String email);
}
