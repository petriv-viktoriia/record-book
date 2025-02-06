package org.pnurecord.recordbook.record;

import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    boolean existsByTitleAndDescription(String title, String description);

    List<Record> findByCategory(Category category);

    List<Record> findByAuthor(User user);

    List<Record> findByStatus(RecordStatus status);

    List<Record> findByAuthorAndStatus(User user, RecordStatus status);

    List<Record> findByPublishedDate(LocalDate publishedDate);
}
