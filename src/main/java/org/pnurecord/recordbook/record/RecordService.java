package org.pnurecord.recordbook.record;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.category.CategoryRepository;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.user.User;
import org.pnurecord.recordbook.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordMapper recordMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;


    public void createRecord(RecordDto recordDto, MultipartFile file) {

        if (recordRepository.existsByTitle(recordDto.getTitle())) {
            throw new DuplicateValueException("Record with title: %s already exists".formatted(recordDto.getTitle()));
        }

        Record record = new Record();
        updateFields(recordDto, file, record);
        record.setPublishedDate(LocalDate.now());

        recordRepository.save(record);
    }

    public void updateRecord(Long id, RecordDto recordDto, MultipartFile file) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Record not found"));
        updateFields(recordDto, file, record);
        recordRepository.save(record);
    }

    private void updateFields(RecordDto recordDto, MultipartFile file, Record record) {
        record.setTitle(recordDto.getTitle());
        record.setDescription(recordDto.getDescription());

        Category category = categoryRepository.findById(recordDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        record.setCategory(category);

        User author = userRepository.findById(recordDto.getAuthorId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        record.setAuthor(author);

        processFile(file, record);

        record.setStatus(RecordStatus.PENDING);
    }

    @SneakyThrows
    private void processFile(MultipartFile file, Record record) {
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            record.setFilename(originalFilename);
            record.setFile(file.getBytes());
        } else {
            throw new NotFoundException("File is empty");
        }
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

    public List<RecordDto> getRecordsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: %s".formatted(categoryId)));

        List<Record> records = recordRepository.findByCategory(category);

        return recordMapper.toRecordDtoList(records);
    }


    public List<RecordDto> getRecordsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: %s".formatted(authorId)));

        List<Record> records = recordRepository.findByAuthor(author);

        return recordMapper.toRecordDtoList(records);
    }

    public List<RecordDto> getRecordsByDate(LocalDate date) {
        return recordMapper.toRecordDtoList(recordRepository.findByPublishedDate(date));
    }

}
