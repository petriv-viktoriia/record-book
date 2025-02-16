package org.pnurecord.recordbook.record;

import jakarta.transaction.Transactional;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    boolean existsByTitle(String title);

    @Query("SELECT r FROM Record r WHERE r.category = :category AND r.status = 'APPROVED'")
    List<Record> findApprovedRecordsByCategory(@Param("category") Category category);

    @Query("SELECT r FROM Record r WHERE r.category = :category AND r.status = 'PENDING'")
    List<Record> findPendingRecordsByCategory(@Param("category") Category category);

    List<Record> findByAuthor(User user);

    List<Record> findByStatus(RecordStatus status);

    List<Record> findByAuthorAndStatus(User user, RecordStatus status);

    @Query("SELECT r FROM Record r WHERE r.status = 'APPROVED' AND r.publishedDate = :publishedDate")
    List<Record> findApprovedRecordsByPublishedDate(
            @Param("publishedDate") LocalDate publishedDate
    );

    @Query("SELECT r FROM Record r WHERE r.status = 'PENDING' AND r.publishedDate = :publishedDate")
    List<Record> findPendingRecordsByPublishedDate(
            @Param("publishedDate") LocalDate publishedDate
    );

    @Query("SELECT r FROM Record r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.status = 'APPROVED'")
    List<Record> findApprovedRecordsByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT r FROM Record r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.status = 'PENDING'")
    List<Record> findPendingRecordsByTitleContainingIgnoreCase(@Param("title") String title);

    @Modifying
    @Transactional
    @Query("UPDATE Record r SET r.author.id = NULL WHERE r.author.id = :authorId")
    void setAuthorIdToNull(@Param("authorId") Long authorId);
}
