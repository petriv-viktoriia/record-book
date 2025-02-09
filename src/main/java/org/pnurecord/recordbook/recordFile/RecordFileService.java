package org.pnurecord.recordbook.recordFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.exceptions.UnsupportedFileTypeException;
import org.pnurecord.recordbook.record.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.pnurecord.recordbook.record.Record;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordFileService {
    private final RecordFileRepository recordFileRepository;
    private final RecordFileMapper recordFileMapper;
    private final RecordRepository recordRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public RecordFileDto getRecordFile(String filename) {
        RecordFile recordFile = recordFileRepository.findByFilename(filename)
                .orElseThrow(() -> new NotFoundException("File not found"));
        return recordFileMapper.toRecordFileDto(recordFile);
    }

    public List<RecordFileInfoDto> getFilesByRecordId(Long recordId, String baseUrl) {
        boolean exists = recordRepository.existsById(recordId);

        if (!exists) {
            throw new NotFoundException("Record not found");
        } else {
            List<RecordFile> files = recordFileRepository.findByRecordId(recordId);
            return files.stream()
                    .map(file -> RecordFileInfoDto.builder()
                            .id(file.getId())
                            .filename(file.getFilename())
                            .type(file.getType())
                            .fileUrl(baseUrl + "/files/" + file.getFilename())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    public void saveRecordFile(MultipartFile file, Long recordId) throws Exception {
        validateFileType(file);

        if (!recordRepository.existsById(recordId)) {
            throw new NotFoundException("Record not found");
        }

        if (recordFileRepository.existsByFilenameIgnoreCaseAndRecordId(file.getOriginalFilename(), recordId)) {
            log.info("File {} already exists for record {}", file.getOriginalFilename(), recordId);
            throw new DuplicateValueException("File with name '%s' for record with id %s already exists".formatted(file.getOriginalFilename(), recordId));
        }

        Record record = new Record();
        record.setId(recordId);

        var recordFile = RecordFile.builder()
                .filename(file.getOriginalFilename())
                .type(file.getContentType())
                .data(file.getBytes())
                .record(record)
                .build();

        recordFileMapper.toRecordFileDto(recordFileRepository.save(recordFile));
    }

    public void deleteRecordFile(String filename) {
        recordFileRepository.deleteByFilename(filename);
    }

    public void deleteRecordFileById(Long id) {
        recordFileRepository.deleteById(id);
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new UnsupportedFileTypeException("Unsupported file type. Allowed formats: JPG, PNG, PDF");
        }
    }
}