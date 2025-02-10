package org.pnurecord.recordbook.record;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.UserCreateDto;
import org.pnurecord.recordbook.user.UserDto;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
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
        UserCreateDto userDto = new UserCreateDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID().toString());
        userDto.setRole(Role.STUDENT);
        savedUser = userService.createUser(userDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);

        recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(savedUser.getId());
        recordDto.setDescription(UUID.randomUUID().toString());
        recordDto.setCategoryId(savedCategory.getId());
    }

    @AfterEach
    void tearDown() {
        recordService.deleteAllRecords();
    }

    @Test
    void testCreateRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);

        assertAll(
                () -> assertNotNull(savedRecord.getId(), "Saved record ID should not be null"),
                () -> assertEquals(recordDto.getTitle(), savedRecord.getTitle(), "Record title should match"),
                () -> assertEquals(recordDto.getDescription(), savedRecord.getDescription(), "Record description should match"),
                () -> assertEquals(savedUser.getId(), savedRecord.getAuthorId(), "Author ID should match"),
                () -> assertEquals(savedCategory.getId(), savedRecord.getCategoryId(), "Category ID should match"),
                () -> assertEquals(RecordStatus.PENDING, savedRecord.getStatus(), "Status should be PENDING"),
                () -> assertEquals(LocalDate.now(), savedRecord.getPublishedDate(), "Published date should be today")
        );
    }

    @Test
    void testUpdateRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);

        RecordDto recordToUpdate = new RecordDto();
        recordToUpdate.setTitle(UUID.randomUUID().toString());
        recordToUpdate.setDescription(UUID.randomUUID().toString());
        recordToUpdate.setCategoryId(savedCategory.getId());
        recordToUpdate.setAuthorId(savedUser.getId());

        RecordDto updatedRecord = recordService.updateRecord(savedRecord.getId(), recordToUpdate);

        assertAll(
                () -> assertEquals(recordToUpdate.getTitle(), updatedRecord.getTitle(), "Title should be updated"),
                () -> assertEquals(recordToUpdate.getDescription(), updatedRecord.getDescription(), "Description should be updated"),
                () -> assertEquals(recordToUpdate.getCategoryId(), updatedRecord.getCategoryId(), "Category should be updated"),
                () -> assertEquals(savedRecord.getAuthorId(), updatedRecord.getAuthorId(), "Author should remain unchanged"),
                () -> assertEquals(savedRecord.getStatus(), updatedRecord.getStatus(), "Status should remain unchanged"),
                () -> assertEquals(savedRecord.getPublishedDate(), updatedRecord.getPublishedDate(), "Published date should remain unchanged")
        );
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
        int numRecords = 5;
        List<RecordDto> savedRecords = new ArrayList<>();
        for (int i = 0; i < numRecords; i++) {
            RecordDto recordDto = new RecordDto();
            recordDto.setTitle(UUID.randomUUID().toString());
            recordDto.setAuthorId(savedUser.getId());
            recordDto.setDescription(UUID.randomUUID().toString());
            recordDto.setCategoryId(savedCategory.getId());
            savedRecords.add(recordService.createRecord(recordDto));
        }

        List<RecordDto> records = recordService.findAllRecords();

        assertEquals(numRecords, records.size(), "The number of records should match the number of records saved");

        for (RecordDto savedRecord : savedRecords) {
            boolean found = records.stream().anyMatch(record -> record.getId().equals(savedRecord.getId()));
            assertTrue(found, "Record with ID " + savedRecord.getId() + " should be in the records list");
        }
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
        assertEquals(savedRecord.getStatus(), RecordStatus.PENDING);
        recordService.approveRecord(savedRecord.getId());

        RecordDto foundById = recordService.findById(savedRecord.getId());
        assertEquals(foundById.getStatus(), RecordStatus.APPROVED);
    }

    @Test
    void testRejectRecord() {
        RecordDto savedRecord = recordService.createRecord(recordDto);
        assertEquals(savedRecord.getStatus(), RecordStatus.PENDING);
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

        RecordDto createdRecord2 = recordService.createRecord(savedRecord2);
        recordService.approveRecord(createdRecord2.getId());

        List<RecordDto> approvedUserRecords = recordService.getRecordsByUserAndStatus(savedUser.getId(), RecordStatus.APPROVED);
        assertEquals(1, approvedUserRecords.size());

        List<RecordDto> pendingUserRecords = recordService.getRecordsByUserAndStatus(savedUser.getId(), RecordStatus.PENDING);
        assertEquals(1, pendingUserRecords.size());
    }


    @Test
    void testFindByCategory() {
        recordService.createRecord(recordDto);

        CategoryDto newCategory = new CategoryDto();
        newCategory.setName(UUID.randomUUID().toString());
        CategoryDto savedNewCategory = categoryService.createCategory(newCategory);

        assertNotNull(savedNewCategory.getId(), "New category should have an ID");

        RecordDto recordDto2 = new RecordDto();
        recordDto2.setAuthorId(savedUser.getId());
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedNewCategory.getId());
        recordService.createRecord(recordDto2);

        RecordDto recordDto3 = new RecordDto();
        recordDto3.setAuthorId(savedUser.getId());
        recordDto3.setTitle(UUID.randomUUID().toString());
        recordDto3.setDescription(UUID.randomUUID().toString());
        recordDto3.setCategoryId(savedNewCategory.getId());
        recordService.createRecord(recordDto3);

        List<RecordDto> foundByCategory = recordService.getRecordsByCategory(savedNewCategory.getId());
        assertEquals(2, foundByCategory.size(), "There should be exactly 2 records in the new category");

        List<RecordDto> foundByCategory2 = recordService.getRecordsByCategory(savedCategory.getId());
        assertEquals(1, foundByCategory2.size(), "There should be exactly 1 record in the saved category");
    }


    @Test
    void testFindByAuthor() {
        recordService.createRecord(recordDto);

        RecordDto recordDto2 = new RecordDto();
        recordDto2.setAuthorId(savedUser.getId());
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());
        recordService.createRecord(recordDto2);

        UserCreateDto userDto2 = new UserCreateDto();
        userDto2.setFirstName(UUID.randomUUID().toString());
        userDto2.setLastName(UUID.randomUUID().toString());
        userDto2.setEmail(UUID.randomUUID().toString());
        userDto2.setRole(Role.STUDENT);
        UserDto savedUser2 = userService.createUser(userDto2);

        RecordDto recordDto3 = new RecordDto();
        recordDto3.setAuthorId(savedUser2.getId());
        recordDto3.setTitle(UUID.randomUUID().toString());
        recordDto3.setDescription(UUID.randomUUID().toString());
        recordDto3.setCategoryId(savedCategory.getId());
        recordService.createRecord(recordDto3);

        List<RecordDto> foundByAuthor1 = recordService.getRecordsByAuthor(savedUser.getId());
        assertEquals(2, foundByAuthor1.size(), "There should be exactly 2 records in the new author");

        List<RecordDto> foundByAuthor2 = recordService.getRecordsByAuthor(savedUser2.getId());
        assertEquals(1, foundByAuthor2.size(), "There should be exactly 1 record in the new author");
    }

    @Test
    void testFindByDate() {
        recordService.createRecord(recordDto);

        RecordDto recordDto2 = new RecordDto();
        recordDto2.setAuthorId(savedUser.getId());
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());

        RecordDto createdRecord2 = recordService.createRecord(recordDto2);
        assertNotNull(createdRecord2.getId(), "RecordDto2 should be successfully created with an ID");

        List<RecordDto> foundByDate = recordService.getRecordsByDate(LocalDate.now());
        assertEquals(2, foundByDate.size(), "There should be exactly 2 records with the date today");
    }


    @Test
    void testFindRecordsByTitle() {
        for (int i = 0; i < 10; i++) {
            RecordDto record = new RecordDto();
            record.setAuthorId(savedUser.getId());
            record.setTitle("Test Title " + i);
            record.setDescription("Description " + i);
            record.setCategoryId(savedCategory.getId());
            recordService.createRecord(record);
        }

        List<RecordDto> foundRecords = recordService.findRecordsByTitle("Test Title", null);

        assertEquals(7, foundRecords.size(), "The number of records should be limited to 7");

        for (RecordDto record : foundRecords) {
            assertTrue(record.getTitle().toLowerCase().contains("test title"),
                    "Each record title should contain the search term (case-insensitive)");
        }
    }
}
