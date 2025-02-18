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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

    public void saveRecordFile(List<MultipartFile> files, Long recordId) throws Exception {
//        if (files == null || files.isEmpty()) {
//            throw new IllegalArgumentException("File list cannot be empty");
//        }

        if (!recordRepository.existsById(recordId)) {
            throw new NotFoundException("Record not found");
        }

        if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
            return;
        }

        Record record = new Record();
        record.setId(recordId);

        List<RecordFile> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFileType(file);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("Original filename cannot be null");
            }

            boolean fileExists = recordFileRepository.findByRecordId(recordId)
                    .stream()
                    .anyMatch(existingFile -> {
                        String existingOriginalName = existingFile.getFilename().substring(existingFile.getFilename().indexOf("_") + 1);
                        return existingOriginalName.equalsIgnoreCase(originalFilename) &&
                                existingFile.getType().equals(file.getContentType());
                    });

            if (fileExists) {
//                throw new DuplicateValueException("File with name '%s' for record with id %s already exists"
//                        .formatted(originalFilename, recordId));
                continue;
            }

            String uuidNumber = UUID.randomUUID().toString();
            RecordFile recordFile = RecordFile.builder()
                    .filename(uuidNumber + "_" + originalFilename)
                    .type(file.getContentType())
                    .data(file.getBytes())
                    .record(record)
                    .build();

            savedFiles.add(recordFile);
        }

        recordFileRepository.saveAll(savedFiles);
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