package org.pnurecord.recordbook.reaction;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReactionServiceTest extends AbstractTestContainerBaseTest {

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private RecordService recordService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    private UserDto savedUser;
    private CategoryDto savedCategory;
    private RecordDto savedRecord;

    private void addReaction(UserDto user, RecordDto record, boolean liked) {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setLiked(liked);
        reactionDto.setUserId(user.getId());
        reactionDto.setRecordId(record.getId());
        reactionService.addReaction(reactionDto);
    }

    private UserDto createUser() {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setFirstName(UUID.randomUUID().toString());
        userDto.setLastName(UUID.randomUUID().toString());
        userDto.setEmail(UUID.randomUUID().toString());
        userDto.setRole(Role.STUDENT);
        return userService.createUser(userDto);
    }

    @BeforeEach
    public void setup() {
        savedUser = createUser();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(UUID.randomUUID().toString());
        savedCategory = categoryService.createCategory(categoryDto);
        savedRecord = recordService.createRecord(createRecordDto(savedUser, savedCategory));
    }

    @AfterEach
    void tearDown() {
        reactionRepository.deleteAll();
    }

    @Test
    void testAddReaction() {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setLiked(true);
        reactionDto.setUserId(savedUser.getId());
        reactionDto.setRecordId(savedRecord.getId());

        ReactionDto savedReaction = reactionService.addReaction(reactionDto);

        assertNotNull(savedReaction.getId(), "Reaction ID should not be null after saving");
        assertEquals(savedUser.getId(), savedReaction.getUserId(), "User ID should match");
        assertEquals(savedRecord.getId(), savedReaction.getRecordId(), "Record ID should match");
        assertTrue(savedReaction.isLiked(), "Liked status should be true");
    }

    @Test
    void testUpdateReaction() {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setLiked(true);
        reactionDto.setUserId(savedUser.getId());
        reactionDto.setRecordId(savedRecord.getId());

        ReactionDto savedReaction = reactionService.addReaction(reactionDto);
        assertNotNull(savedReaction.getId(), "Reaction ID should not be null after saving");

        ReactionUpdateDto updateDto = new ReactionUpdateDto();
        updateDto.setLiked(false);

        ReactionDto updatedReaction = reactionService.updateReaction(savedReaction.getId(), updateDto);
        assertNotNull(updatedReaction, "Updated reaction should not be null");
        assertEquals(savedReaction.getId(), updatedReaction.getId(), "The ID should remain the same after update");
        assertFalse(updatedReaction.isLiked(), "The liked status should be false after update");
    }

    @Test
    void testDeleteReaction() {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setLiked(true);
        reactionDto.setUserId(savedUser.getId());
        reactionDto.setRecordId(savedRecord.getId());

        ReactionDto savedReaction = reactionService.addReaction(reactionDto);
        assertNotNull(savedReaction.getId(), "Reaction should be saved successfully");

        reactionService.deleteReaction(savedReaction.getId());
        assertFalse(reactionRepository.findById(savedReaction.getId()).isPresent(), "Reaction should be deleted");
    }

    @Test
    void testGetReactionsCount() {
        ReactionDto reactionDto1 = new ReactionDto();
        reactionDto1.setLiked(true);
        reactionDto1.setUserId(savedUser.getId());
        reactionDto1.setRecordId(savedRecord.getId());
        reactionService.addReaction(reactionDto1);

        UserDto userDto2 = createUser();
        ReactionDto reactionDto2 = new ReactionDto();
        reactionDto2.setLiked(false);
        reactionDto2.setUserId(userDto2.getId());
        reactionDto2.setRecordId(savedRecord.getId());
        reactionService.addReaction(reactionDto2);

        ReactionCountDto reactionsCountDto = reactionService.getReactionsCount(savedRecord.getId());
        assertNotNull(reactionsCountDto, "Reaction count should not be null");
        assertEquals(1, reactionsCountDto.getLikes(), "There should be exactly 1 like");
        assertEquals(1, reactionsCountDto.getDislikes(), "There should be exactly 1 dislike");
    }

    @Test
    void testGetAllReactions() {
        RecordDto recordDto2 = new RecordDto();
        recordDto2.setTitle(UUID.randomUUID().toString());
        recordDto2.setAuthorId(savedUser.getId());
        recordDto2.setDescription(UUID.randomUUID().toString());
        recordDto2.setCategoryId(savedCategory.getId());
        RecordDto savedRecord2 = recordService.createRecord(recordDto2);

        addReaction(savedUser, savedRecord, true);
        UserDto savedUser2 = createUser();
        addReaction(savedUser2, savedRecord2, false);

        List<ReactionDto> reactions = reactionService.getReactions();
        assertNotNull(reactions, "Reactions list should not be null");
        assertEquals(2, reactions.size(), "There should be exactly 2 reactions");
    }

    @Test
    void testFindReactionById() {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setLiked(true);
        reactionDto.setUserId(savedUser.getId());
        reactionDto.setRecordId(savedRecord.getId());

        ReactionDto savedReaction = reactionService.addReaction(reactionDto);
        assertNotNull(savedReaction.getId(), "Reaction ID should not be null after saving");

        ReactionDto foundById = reactionService.getReactionById(savedReaction.getId());

        assertNotNull(foundById, "Found reaction should not be null");
        assertNotNull(foundById.getId(), "Reaction ID should not be null in the found reaction");
        assertEquals(savedReaction.getId(), foundById.getId(), "Reaction ID should match");
        assertEquals(savedReaction.getRecordId(), foundById.getRecordId(), "Record ID should match");
        assertEquals(savedReaction.isLiked(), foundById.isLiked(), "Liked status should match");
        assertEquals(savedReaction.getUserId(), foundById.getUserId(), "User ID should match");
    }


    private RecordDto createRecordDto(UserDto user, CategoryDto category) {
        RecordDto recordDto = new RecordDto();
        recordDto.setTitle(UUID.randomUUID().toString());
        recordDto.setAuthorId(user.getId());
        recordDto.setDescription(UUID.randomUUID().toString());
        recordDto.setCategoryId(category.getId());
        return recordDto;
    }
}
