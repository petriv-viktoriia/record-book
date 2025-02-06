package org.pnurecord.recordbook.reaction;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Reaction findByRecordIdAndUserId(long recordId, long userId);
    boolean existsByRecordIdAndUserId(long recordId, long userId);

    int countByRecordIdAndLiked(long recordId, boolean liked);
}
