package org.pnurecord.recordbook.record;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.category.CategoryRepository;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.User;
import org.pnurecord.recordbook.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordMapper recordMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void nullifyAuthorReferences(Long authorId) {
        recordRepository.setAuthorIdToNull(authorId);
    }

    public RecordDto createRecord(RecordDto recordDto) {

        if (recordRepository.existsByTitle(recordDto.getTitle())) {
            throw new DuplicateValueException("Record with title: %s already exists".formatted(recordDto.getTitle()));
        }

        Record record = new Record();
        record.setTitle(recordDto.getTitle());
        record.setDescription(recordDto.getDescription());

        Category category = categoryRepository.findById(recordDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        record.setCategory(category);

        User author = userRepository.findById(recordDto.getAuthorId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        record.setAuthor(author);

        if (author.getRole().equals(Role.ADMIN)) {
            record.setStatus(RecordStatus.APPROVED);
        } else if (author.getRole().equals(Role.STUDENT)) {
            record.setStatus(RecordStatus.PENDING);
        }
        record.setPublishedDate(LocalDate.now());

        return recordMapper.toRecordDto(recordRepository.save(record));
    }

    public RecordDto updateRecord(Long id, RecordDto recordDto) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Record not found"));

        if (!record.getTitle().equals(recordDto.getTitle()) &&
                recordRepository.existsByTitle(recordDto.getTitle())) {
            throw new DuplicateValueException("Record with title: %s already exists"
                    .formatted(recordDto.getTitle()));
        }

        record.setTitle(recordDto.getTitle());
        record.setDescription(recordDto.getDescription());

        Category category = categoryRepository.findById(recordDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        record.setCategory(category);

        return recordMapper.toRecordDto(recordRepository.save(record));
    }

    public List<RecordDto> findAllRecords() {
        return recordMapper.toRecordDtoList(recordRepository.findAll());
    }

    public RecordDto findById(long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Record not found with id: %s".formatted(recordId)));
        return recordMapper.toRecordDto(record);
    }

    public void deleteAllRecords() {
        recordRepository.deleteAll();
    }

    public void deleteRecord(Long recordId) {
        if (!recordRepository.existsById(recordId)) {
            throw new NotFoundException("Record not found with id: %s".formatted(recordId));
        }
        recordRepository.deleteById(recordId);
    }

    //додаткові методи

    public List<RecordDto> findAllApprovedRecords() {
        List<Record> approvedRecords = recordRepository.findByStatus(RecordStatus.APPROVED);
        return recordMapper.toRecordDtoList(approvedRecords);
    }

    public List<RecordDto> findAllPendingRecords() {
        List<Record> pendingRecords = recordRepository.findByStatus(RecordStatus.PENDING);
        return recordMapper.toRecordDtoList(pendingRecords);
    }

    public List<RecordDto> getRecordsByUserAndStatus(Long userId, RecordStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Record> records = recordRepository.findByAuthorAndStatus(user, status);
        return recordMapper.toRecordDtoList(records);
    }


    public void approveRecord(Long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Record not found with id: %s".formatted(recordId)));

        record.setStatus(RecordStatus.APPROVED);
        recordRepository.save(record);
    }

    public void rejectRecord(Long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Record not found with id: %s".formatted(recordId)));

        record.setStatus(RecordStatus.REJECTED);
        recordRepository.save(record);
    }

    public List<RecordDto> getApprovedRecordsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: %s".formatted(categoryId)));

        List<Record> records = recordRepository.findApprovedRecordsByCategory(category);

        return recordMapper.toRecordDtoList(records);
    }

    public List<RecordDto> getPendingRecordsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: %s".formatted(categoryId)));

        List<Record> records = recordRepository.findPendingRecordsByCategory(category);

        return recordMapper.toRecordDtoList(records);
    }


    public List<RecordDto> getRecordsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: %s".formatted(authorId)));

        List<Record> records = recordRepository.findByAuthor(author);

        return recordMapper.toRecordDtoList(records);
    }

    public List<RecordDto> getApprovedRecordsByDate(LocalDate date) {
        return recordMapper.toRecordDtoList(recordRepository.findApprovedRecordsByPublishedDate(date));
    }

    public List<RecordDto> getPendingRecordsByDate(LocalDate date) {
        return recordMapper.toRecordDtoList(recordRepository.findPendingRecordsByPublishedDate(date));
    }


    public List<RecordDto> findApprovedRecordsByTitle(String title, Integer limit) {
        int defaultLimit = 20;
        int searchLimit = limit != null ? limit : defaultLimit;

        List<Record> records = recordRepository.findApprovedRecordsByTitleContainingIgnoreCase(title);
        if (records.size() > searchLimit) {
            records = records.subList(0, searchLimit);
        }
        return recordMapper.toRecordDtoList(records);
    }

    public List<RecordDto> findPendingRecordsByTitle(String title, Integer limit) {
        int defaultLimit = 20;
        int searchLimit = limit != null ? limit : defaultLimit;

        List<Record> records = recordRepository.findPendingRecordsByTitleContainingIgnoreCase(title);
        if (records.size() > searchLimit) {
            records = records.subList(0, searchLimit);
        }
        return recordMapper.toRecordDtoList(records);
    }

}
