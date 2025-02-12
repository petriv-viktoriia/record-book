package org.pnurecord.recordbook.recordFile;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordFileRepository extends CrudRepository<RecordFile, Long> {
    Optional<RecordFile> findByFilename(String filename);

    boolean existsByFilenameIgnoreCaseAndRecordId(String filename, Long recordId);

    @Query("SELECT rf.filename FROM RecordFile rf WHERE rf.record.id = :recordId")
    List<String> findFilenamesByRecordId(@Param("recordId") Long recordId);

    @Modifying
    @Transactional
    @Query("delete from RecordFile rf where rf.filename = :filename")
    void deleteByFilename(@Param("filename") String filename);

    List<RecordFile> findByRecordId(Long recordId);

    @Transactional
    void deleteByRecordId(Long recordId);
}
