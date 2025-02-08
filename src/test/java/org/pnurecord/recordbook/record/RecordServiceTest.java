package org.pnurecord.recordbook.record;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.UserDto;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecordServiceTest extends AbstractTestContainerBaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecordService recordService;

    private UserDto savedUser;
    private CategoryDto savedCategory;
    private RecordDto recordDto;


    @BeforeEach
    public void setup() {
        UserDto userDto = new UserDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID().toString());
        userDto.setRole(Role.GUEST);
        savedUser = userService.save(userDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);

        recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(savedUser.getId());
        recordDto.setDescription(UUID.randomUUID().toString());
        recordDto.setCategoryId(savedCategory.getId());
        recordDto.setStatus(RecordStatus.PENDING);
        recordDto.setPublishedDate(LocalDate.now());
    }

    @AfterEach
    void tearDown(){
        recordService.deleteAllRecords();
    }

    @Test
    void testCreateRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);

        assertNotNull(savedRecord.getId(), "Saved record ID should not be null");

        assertEquals(recordDto.getTitle(), savedRecord.getTitle(), "Record title should match the expected value");
        assertEquals(recordDto.getDescription(), savedRecord.getDescription(), "Record description should match the expected value");
        assertEquals(savedUser.getId(), savedRecord.getAuthorId(), "Author ID should match the expected value");
        assertEquals(savedCategory.getId(), savedRecord.getCategoryId(), "Category ID should match the expected value");
        assertEquals(recordDto.getStatus(), savedRecord.getStatus(), "Record status should match the expected value");
        assertEquals(recordDto.getPublishedDate(), savedRecord.getPublishedDate(), "Record published date should match the expected value");
    }



    @Test
    void testUpdateRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);

        RecordDto recordToUpdate = new RecordDto();
        recordToUpdate.setTitle(UUID.randomUUID().toString());
        recordToUpdate.setAuthorId(savedUser.getId());
        recordToUpdate.setDescription(UUID.randomUUID().toString());
        recordToUpdate.setCategoryId(savedCategory.getId());
        recordToUpdate.setStatus(RecordStatus.PENDING);
        recordToUpdate.setPublishedDate(LocalDate.now());

        RecordDto updatedRecord = recordService.updateRecord(savedRecord.getId(), recordToUpdate);

        assertEquals(savedRecord.getId(), updatedRecord.getId(), "Record ID should remain unchanged after update");
        assertEquals(recordToUpdate.getTitle(), updatedRecord.getTitle(), "Title should be updated");
        assertEquals(recordToUpdate.getDescription(), updatedRecord.getDescription(), "Description should be updated");
        assertEquals(savedUser.getId(), updatedRecord.getAuthorId(), "Author ID should remain unchanged");
        assertEquals(savedCategory.getId(), updatedRecord.getCategoryId(), "Category ID should remain unchanged");
        assertEquals(recordToUpdate.getStatus(), updatedRecord.getStatus(), "Status should be updated");
        assertEquals(recordToUpdate.getPublishedDate(), updatedRecord.getPublishedDate(), "Published date should be updated");
    }

    @Test
    void testGetRecordById() {
        RecordDto savedRecord = recordService.createRecord(recordDto);

        RecordDto foundById = recordService.findById(savedRecord.getId());
        assertEquals(savedRecord.getId(), foundById.getId());
        assertEquals(savedRecord.getTitle(), foundById.getTitle());
        assertEquals(savedRecord.getDescription(), foundById.getDescription());
        assertEquals(savedRecord.getAuthorId(), foundById.getAuthorId());
        assertEquals(savedRecord.getCategoryId(), foundById.getCategoryId());
        assertEquals(savedRecord.getStatus(), foundById.getStatus());
        assertEquals(savedRecord.getPublishedDate(), foundById.getPublishedDate());
    }

    @Test
    void testFindAllRecords() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        List<RecordDto> records = recordService.findAllRecords();

        assertEquals(1, records.size(), "There should be exactly 1 record in the list");

        assertEquals(savedRecord.getId(), records.get(0).getId(), "The record ID should match the saved record's ID");
    }


    @Test
    void testDeleteRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        assertNotNull(savedRecord.getId(), "Saved record should have an ID");

        recordService.deleteRecord(savedRecord.getId());

        assertThrows(NotFoundException.class, () -> {
            recordService.findById(savedRecord.getId());
        }, "Record should throw NotFoundException when searched after deletion");
    }

    @Test
    void testDeleteAllRecords() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        assertNotNull(savedRecord.getId(), "Saved record should have an ID");

        recordService.deleteAllRecords();

        List<RecordDto> allRecords = recordService.findAllRecords();
        assertTrue(allRecords.isEmpty(), "All records should be deleted, and the list should be empty");
    }

    @Test
    void testApproveRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        recordService.approveRecord(savedRecord.getId());

        RecordDto foundById = recordService.findById(savedRecord.getId());
        assertEquals(foundById.getStatus(), RecordStatus.APPROVED);
    }

    @Test
    void testRejectRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        recordService.rejectRecord(savedRecord.getId());

        RecordDto foundById = recordService.findById(savedRecord.getId());
        assertEquals(foundById.getStatus(), RecordStatus.REJECTED);
    }


    @Test
    void findAllApprovedPendingRecords() {
        RecordDto savedRecordPending = recordService.createRecord(recordDto);

        RecordDto recordApproved = new RecordDto();
        recordApproved.setTitle(UUID.randomUUID().toString());
        recordApproved.setAuthorId(savedUser.getId());
        recordApproved.setDescription(UUID.randomUUID().toString());
        recordApproved.setCategoryId(savedCategory.getId());
        recordApproved.setPublishedDate(LocalDate.now());
        RecordDto savedRecordApproved = recordService.createRecord(recordApproved);
        recordService.approveRecord(savedRecordApproved.getId());

        List<RecordDto> records = recordService.findAllRecords();
        assertEquals(2, records.size(), "There should be exactly 2 records in the list");

        List<RecordDto> approvedRecords = recordService.findAllApprovedRecords();
        assertEquals(1, approvedRecords.size(), "There should be exactly 1 approved record in the list");
        assertEquals(savedRecordApproved.getId(), approvedRecords.get(0).getId(), "Approved record should have the same ID");

        List<RecordDto> pendingRecords = recordService.findAllPendingRecords();
        assertEquals(1, pendingRecords.size(), "There should be exactly 1 pending record in the list");
        assertEquals(savedRecordPending.getId(), pendingRecords.get(0).getId(), "Pending record should have the same ID");
    }

    @Test
    void testFindByUserAndStatus() {
        recordService.createRecord(recordDto);

        RecordDto savedRecord2 = new RecordDto();
        savedRecord2.setAuthorId(savedUser.getId());
        savedRecord2.setTitle(UUID.randomUUID().toString());
        savedRecord2.setDescription(UUID.randomUUID().toString());
        savedRecord2.setCategoryId(savedCategory.getId());
        savedRecord2.setStatus(RecordStatus.PENDING);
        savedRecord2.setPublishedDate(LocalDate.now());

        RecordDto createdRecord2 = recordService.createRecord(savedRecord2);
        recordService.approveRecord(createdRecord2.getId());

        List<RecordDto> approvedUserRecords = recordService.getRecordsByUserAndStatus(savedUser.getId(), RecordStatus.APPROVED);

        assertEquals(1, approvedUserRecords.size(), "Очікується 2 схвалених записи для користувача");
    }

}
