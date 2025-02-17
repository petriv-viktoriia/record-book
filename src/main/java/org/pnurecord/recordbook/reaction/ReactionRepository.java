package org.pnurecord.recordbook.reaction;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Reaction findByRecordIdAndUserId(long recordId, long userId);
    boolean existsByRecordIdAndUserId(long recordId, long userId);

    int countByRecordIdAndLiked(long recordId, boolean liked);


    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.record.id = :recordId")
    void deleteByRecordId(@Param("recordId") Long recordId);
}
