package org.pnurecord.recordbook.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.category.CategoryRepository;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.reaction.ReactionCountDto;
import org.pnurecord.recordbook.reaction.ReactionService;
import org.pnurecord.recordbook.recordFile.RecordFileInfoDto;
import org.pnurecord.recordbook.recordFile.RecordFileRepository;
import org.pnurecord.recordbook.recordFile.RecordFileService;
import org.pnurecord.recordbook.user.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/web/records")
public class RecordWebController {

    private final RecordFileService recordFileService;
    private final RecordService recordService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final RecordFileRepository recordFileRepository;
    private final ReactionService reactionService;

    //-----------for guests-------------
    @GetMapping()
    public String showAllApprovedRecords(Model model) {
        List<RecordDto> records = recordService.findAllApprovedRecords();
        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, ReactionCountDto> reactions = new HashMap<>();

        for (RecordDto record : records) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));

            ReactionCountDto reactionCount = reactionService.getReactionsCount(record.getId());
            reactions.put(record.getId(), reactionCount);
        }

        model.addAttribute("reactions", reactions);
        model.addAttribute("records", records);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "records/listApproved2";
    }
    //-------------------------------------

    @GetMapping("/all")
    public String showAllRecords(Model model) {
        List<RecordDto> allRecords = recordService.findAllRecords();

        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, ReactionCountDto> reactions = new HashMap<>();


        for (RecordDto record : allRecords) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));

            ReactionCountDto reactionCount = reactionService.getReactionsCount(record.getId());
            reactions.put(record.getId(), reactionCount);
        }


        model.addAttribute("reactions", reactions);
        model.addAttribute("allRecords", allRecords);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);

        return "records/listAllRecords";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recordDto", new RecordDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "records/form2";
    }

    @PostMapping("/create")
    public String createRecord(@ModelAttribute @Valid RecordDto recordDto,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "records/form2";
        }

        try {
//            //needs to be changed with security
//
//            UserCreateDto newUser = new UserCreateDto();
//            newUser.setRole(Role.STUDENT);
//            newUser.setFirstName(UUID.randomUUID().toString());
//            newUser.setLastName(UUID.randomUUID().toString());
//            newUser.setEmail(UUID.randomUUID() + "@test.com");
//
//            UserDto userDto = userService.createUser(newUser);
//            recordDto.setAuthorId(userDto.getId());

            RecordDto createdRecord = recordService.createRecord(recordDto);

            if (file != null && !file.isEmpty()) {
                recordFileService.saveRecordFile(file, createdRecord.getId());
                redirectAttributes.addFlashAttribute("message", "Record created and file uploaded successfully.");
            } else {
                redirectAttributes.addFlashAttribute("message", "Record created successfully.");
            }

            return "redirect:/web/records/" + createdRecord.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create record: " + e.getMessage());
            return "redirect:/web/records/create";
        }
    }


    @GetMapping("/edit/{recordId}")
    public String showEditForm(@PathVariable Long recordId, Model model) {
        RecordDto recordDto = recordService.findById(recordId);
        model.addAttribute("recordDto", recordDto);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "records/form2";
    }

    @PutMapping("/edit/{recordId}")
    public String updateRecord(@PathVariable Long recordId,
                               @ModelAttribute @Valid RecordDto recordDto,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "records/form";
        }

        try {
            recordService.updateRecord(recordId, recordDto);

            if (file != null && !file.isEmpty()) {
                recordFileRepository.deleteByRecordId(recordId);
                recordFileService.saveRecordFile(file, recordId);
                redirectAttributes.addFlashAttribute("message", "Record updated and new file uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("message", "Record updated successfully");
            }

            return "redirect:/web/records/" + recordId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update record: " + e.getMessage());
            return "redirect:/web/records/edit/" + recordId;
        }
    }


    @DeleteMapping("/delete/{recordId}")
    public String deleteRecord(@PathVariable Long recordId, RedirectAttributes redirectAttributes) {
        try {
            recordService.deleteRecord(recordId);
            redirectAttributes.addFlashAttribute("message", "Record successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/web/records";
    }


    @GetMapping("/{id}")
    public String getRecordDetails(@PathVariable Long id, Model model) {
        RecordDto record = recordService.findById(id);
        String categoryName = categoryService.findById(record.getCategoryId()).getName();

        var author = userService.getUserById(record.getAuthorId());
        String authorName = author.getFirstName() + " " + author.getLastName();

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(id, baseUrl);

        model.addAttribute("record", record);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("authorName", authorName);
        model.addAttribute("files", files);

        return "records/details2";
    }

    @GetMapping("/pending")
    public String showPendingRecords(Model model) {
        List<RecordDto> records = recordService.findAllPendingRecords();
        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (RecordDto record : records) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));
        }


        model.addAttribute("records", records);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "records/listPending2";
    }


    @GetMapping("/search")
    public String searchApprovedRecords(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit,
            Model model) {
        List<RecordDto> searchResults = Collections.emptyList();

        if (title != null && title.length() >= 2) {
            searchResults = recordService.findApprovedRecordsByTitle(title, limit);
        }

        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (RecordDto record : searchResults) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));
        }

        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("records", searchResults);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchLimit", limit);

        return "records/searchResults";
    }

    @GetMapping("/pending/search")
    public String searchPendingRecords(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit,
            Model model) {
        List<RecordDto> searchResults = Collections.emptyList();

        if (title != null && title.length() >= 2) {
            searchResults = recordService.findPendingRecordsByTitle(title, limit);
        }

        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (RecordDto record : searchResults) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));
        }

        model.addAttribute("records", searchResults);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("records", searchResults);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchLimit", limit);

        return "records/searchResults";
    }

    @PostMapping("/{recordId}/approve")
    public String approveRecord(
            @PathVariable Long recordId,
            RedirectAttributes redirectAttributes) {
        try {
            recordService.approveRecord(recordId);
            redirectAttributes.addFlashAttribute("successMessage", "Record successfully approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve record");
        }
        return "redirect:/web/records/" + recordId;
    }

    @PostMapping("/{recordId}/reject")
    public String rejectRecord(
            @PathVariable Long recordId,
            RedirectAttributes redirectAttributes) {
        try {
            recordService.rejectRecord(recordId);
            redirectAttributes.addFlashAttribute("successMessage", "Record successfully rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject record");
        }
        return "redirect:/web/records/" + recordId;
    }

    @GetMapping("/date/{date}")
    public String getApprovedRecordsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getApprovedRecordsByDate(date);

            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
            }
            model.addAttribute("records", records);
            model.addAttribute("selectedDate", date);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the specified date");
            return "redirect:/web/records";
        }
    }

    @GetMapping("/pending/date/{date}")
    public String getPendingRecordsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getPendingRecordsByDate(date);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
            }
            model.addAttribute("records", records);
            model.addAttribute("selectedDate", date);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the specified date");
            return "redirect:/web/records";
        }
    }


    @GetMapping("/categories/{categoryId}")
    public String getApprovedRecordsByCategory(
            @PathVariable Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getApprovedRecordsByCategory(categoryId);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
            }
            model.addAttribute("records", records);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the selected category.");
            return "redirect:/web/records";
        }
    }

    @GetMapping("/pending/categories/{categoryId}")
    public String getPendingRecordsByCategory(
            @PathVariable Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getPendingRecordsByCategory(categoryId);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
            }
            model.addAttribute("records", records);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the selected category.");
            return "redirect:/web/records";
        }
    }


    @GetMapping("/users/{userId}/status")
    public String getUserRecordsByStatus(
            @PathVariable Long userId,
            @RequestParam RecordStatus status,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getRecordsByUserAndStatus(userId, status);
            UserDto user = userService.getUserById(userId);

            model.addAttribute("records", records);
            model.addAttribute("user", user);
            model.addAttribute("status", status);
            return "records/listByUserStatus";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to fetch records for the specified user and status.");
            return "redirect:/web/records";
        }
    }

    @GetMapping("/authors/{authorId}")
    public String getRecordsByAuthor(
            @PathVariable Long authorId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getRecordsByAuthor(authorId);
            UserDto author = userService.getUserById(authorId);

            model.addAttribute("records", records);
            model.addAttribute("author", author);
            return "records/authorRecords";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to fetch records for the specified author.");
            return "redirect:/web/records";
        }
    }

}
