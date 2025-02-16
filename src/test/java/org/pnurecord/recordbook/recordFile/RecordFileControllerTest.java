package org.pnurecord.recordbook.recordFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pnurecord.recordbook.AbstractTestContainerBaseTest;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.record.RecordDto;
import org.pnurecord.recordbook.record.RecordService;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.UserCreateDto;
import org.pnurecord.recordbook.user.UserDto;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecordFileControllerTest extends AbstractTestContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecordService recordService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordFileService recordFileService;

    private UserDto savedUser;
    private CategoryDto savedCategory;
    private RecordDto savedRecord;
    private MockMultipartFile file;

    @BeforeEach
    public void setup() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID() + "@test.com");
        userDto.setRole(Role.STUDENT);
        savedUser = userService.createUser(userDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);

        RecordDto recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(savedUser.getId());
        recordDto.setDescription("Test Description");
        recordDto.setCategoryId(savedCategory.getId());
        savedRecord = recordService.createRecord(recordDto);

        file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateFile() throws Exception {
        mockMvc.perform(multipart("/files/upload/{recordId}", savedRecord.getId())
                        .file(file))
                .andExpect(status().isOk());

        List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(
                savedRecord.getId(),
                "http://localhost:8080"
        );
        assertEquals(1, files.size(), "Should have exactly one file");
        assertEquals("test.jpg", files.get(0).getFilename(), "Filename should match uploaded file");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testReturnFile() throws Exception {
        mockMvc.perform(multipart("/files/upload/{recordId}", savedRecord.getId())
                        .file(file))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/files/{filename}", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_TYPE))
                .andReturn();

        String contentType = result.getResponse().getHeader(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentType, "Content-Type should not be null");

        byte[] fileContent = result.getResponse().getContentAsByteArray();
        assertTrue(fileContent.length > 0, "File content should not be empty");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnFileInfoByRecordId() throws Exception {
        mockMvc.perform(multipart("/files/upload/{recordId}", savedRecord.getId())
                        .file(file))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/files/info/{recordId}", savedRecord.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<RecordFileInfoDto> fileInfoList = objectMapper.readValue(responseContent, new TypeReference<List<RecordFileInfoDto>>() {
        });

        assertEquals(1, fileInfoList.size(), "There should be exactly one file information returned");
        RecordFileInfoDto fileInfo = fileInfoList.get(0);
        assertEquals("test.jpg", fileInfo.getFilename(), "Filename should match uploaded file");
        assertTrue(fileInfo.getFileUrl().contains("/files/test.jpg"), "File URL should contain the correct filename");
        assertEquals("image/jpeg", fileInfo.getType(), "File type should be image/jpeg");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteFileByFilename() throws Exception {
        mockMvc.perform(multipart("/files/upload/{recordId}", savedRecord.getId())
                        .file(file))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/files/name/{filename}", "test.jpg"))
                .andExpect(status().isOk());

        List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(
                savedRecord.getId(),
                "http://localhost:8080"
        );

        assertEquals(0, files.size(), "The file should be deleted and no files should remain");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteFileById() throws Exception {
       mockMvc.perform(multipart("/files/upload/{recordId}", savedRecord.getId())
                        .file(file))
                .andExpect(status().isOk());

        List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(savedRecord.getId(), "http://localhost:8080");
        Long fileId = files.get(0).getId();

        mockMvc.perform(delete("/files/{id}", fileId))
                .andExpect(status().isOk());

        List<RecordFileInfoDto> remainingFiles = recordFileService.getFilesByRecordId(
                savedRecord.getId(),
                "http://localhost:8080"
        );
        assertEquals(0, remainingFiles.size(), "The file should be deleted and no files should remain");
    }

}
