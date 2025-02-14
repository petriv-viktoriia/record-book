package org.pnurecord.recordbook.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/records")
public class RecordController {
    private final RecordService recordService;

    @GetMapping
    public List<RecordDto> findAllRecords() {
        return recordService.findAllRecords();
    }

    @GetMapping("/{recordId}")
    public RecordDto getRecordById(@PathVariable Long recordId) {
        return recordService.findById(recordId);
    }

    @PostMapping
    public RecordDto createRecord(@Valid @RequestBody RecordDto recordDto) {
        return recordService.createRecord(recordDto);
    }

    @PutMapping("/{recordId}")
    public RecordDto updateRecord(@PathVariable Long recordId, @Valid @RequestBody RecordDto recordDto) {
        return recordService.updateRecord(recordId, recordDto);
    }

    @DeleteMapping("/{recordId}")
    public void deleteRecordById(@PathVariable Long recordId) {
        recordService.deleteRecord(recordId);
    }

    @DeleteMapping
    public void deleteAllRecords() {
        recordService.deleteAllRecords();
    }

    @GetMapping("/categories/{categoryId}")
    public List<RecordDto> getRecordsByCategory(@PathVariable Long categoryId) {
        return recordService.getRecordsByCategory(categoryId);
    }

    @GetMapping("/authors/{authorId}")
    public List<RecordDto> getRecordsByAuthor(@PathVariable Long authorId) {
        return recordService.getRecordsByAuthor(authorId);
    }

    @GetMapping("/approved")
    public List<RecordDto> findAllApprovedRecords() {
        return recordService.findAllApprovedRecords();
    }

    @GetMapping("/pending")
    public List<RecordDto> findAllPendingRecords() {
        return recordService.findAllPendingRecords();
    }

    @GetMapping("/date/{date}")
    public List<RecordDto> getRecordsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return recordService.getRecordsByDate(date);
    }

    @GetMapping("/users/{userId}")
    public List<RecordDto> getUserRecordsByStatus(@PathVariable Long userId, @RequestParam RecordStatus status) {
        return recordService.getRecordsByUserAndStatus(userId, status);
    }

    @PutMapping("/{recordId}/approve")
    public void approveRecord(@PathVariable Long recordId) {
        recordService.approveRecord(recordId);
    }

    @PutMapping("/{recordId}/reject")
    public void rejectRecord(@PathVariable Long recordId) {
        recordService.rejectRecord(recordId);
    }

    @GetMapping("/search")
    public List<RecordDto> searchRecordsByTitle(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit) {
        if (title.length() < 2) {
            return Collections.emptyList();
        }
        return recordService.findRecordsByTitle(title, limit);
    }
}
