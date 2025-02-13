package org.pnurecord.recordbook.record;

import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    boolean existsByTitle(String title);

    List<Record> findApprovedRecordsByCategory(Category category);
    List<Record> findPendingRecordsByCategory(Category category);

    List<Record> findByAuthor(User user);

    List<Record> findByStatus(RecordStatus status);

    List<Record> findByAuthorAndStatus(User user, RecordStatus status);

    List<Record> findApprovedRecordsByPublishedDate(LocalDate publishedDate);
    List<Record> findPendingRecordsByPublishedDate(LocalDate publishedDate);

    @Query("SELECT r FROM Record r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.status = 'APPROVED'")
    List<Record> findApprovedRecordsByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT r FROM Record r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.status = 'PENDING'")
    List<Record> findPendingRecordsByTitleContainingIgnoreCase(@Param("title") String title);

}
