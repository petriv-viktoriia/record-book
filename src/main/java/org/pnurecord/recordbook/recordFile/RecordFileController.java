package org.pnurecord.recordbook.recordFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class RecordFileController {
    private final RecordFileService recordFileService;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getRecordFile(@PathVariable String filename) {
        var recordFile = recordFileService.getRecordFile(filename);
        var body = new ByteArrayResource(recordFile.getData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, recordFile.getType())
                .body(body);
    }

    @GetMapping("/info/{recordId}")
    public List<RecordFileInfoDto> getFileInfoByRecordId(@PathVariable Long recordId) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return recordFileService.getFilesByRecordId(recordId, baseUrl);
    }

    @PostMapping("/upload/{recordId}")
    public void upload(
            @PathVariable Long recordId,
            @RequestPart MultipartFile file) {
        try {
            recordFileService.saveRecordFile(file, recordId);
        } catch (Exception e) {
            log.error("File upload error: {}", e.getMessage());
            throw new RuntimeException("File upload error", e);
        }
    }

    @DeleteMapping("/name/{filename}")
    public void deleteFile(@PathVariable String filename) {
        recordFileService.deleteRecordFile(filename);
    }

    @DeleteMapping("/{id}")
    public void deleteFileById(@PathVariable Long id) {
        recordFileService.deleteRecordFileById(id);
    }
}