package org.pnurecord.recordbook.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.category.CategoryDto;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.recordFile.RecordFileInfoDto;
import org.pnurecord.recordbook.recordFile.RecordFileRepository;
import org.pnurecord.recordbook.recordFile.RecordFileService;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.UserCreateDto;
import org.pnurecord.recordbook.user.UserDto;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/web/records")
public class RecordWebController {

    private final RecordFileService recordFileService;
    private final RecordService recordService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RecordFileRepository recordFileRepository;

    //-----------for guests-------------
    @GetMapping("/approved")
    public String showAllApprovedRecords(Model model) {
        List<RecordDto> approved = recordService.findAllApprovedRecords();
        for (RecordDto recordDto : approved) {
            UserDto author = userService.getUserById(recordDto.getAuthorId());
            recordDto.setAuthorName(author.getFirstName() + " " + author.getLastName());

            CategoryDto categoryDto = categoryService.findById(recordDto.getCategoryId());
            recordDto.setCategoryName(categoryDto.getName());
        }
        model.addAttribute("approved", approved);
        return "records/listApproved";
    }
    //-------------------------------------


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recordDto", new RecordDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "records/form";
    }

    @PostMapping("/new")
    public String createRecord(@ModelAttribute @Valid RecordDto recordDto,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "records/form";
        }

        try {
//            //needs to be changed with security

//            UserCreateDto newUser = new UserCreateDto();
//            newUser.setRole(Role.ADMIN);
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
            return "redirect:/web/records/new";
        }
    }


    @GetMapping("/edit/{recordId}")
    public String showEditForm(@PathVariable Long recordId, Model model) {
        RecordDto recordDto = recordService.findById(recordId);
        model.addAttribute("recordDto", recordDto);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "records/form";
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
        return "redirect:/web/records/approved";
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

        return "records/details";
    }

    @GetMapping("/pending")
    public String showPendingRecords(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> pending = recordService.findAllPendingRecords();
            model.addAttribute("pending", pending);
            return "records/listPending";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch pending records.");
            return "redirect:/web/records/approved";
        }
    }


    @GetMapping("/search")
    public String searchRecords(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit,
            Model model) {
        List<RecordDto> searchResults = Collections.emptyList();

        if (title != null && title.length() >= 2) {
            searchResults = recordService.findRecordsByTitle(title, limit);
        }

        model.addAttribute("records", searchResults);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchLimit", limit);

        return "records/listApproved"; // needs to be used in main page
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
    public String getRecordsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getRecordsByDate(date);
            model.addAttribute("records", records);
            model.addAttribute("selectedDate", date);
            return "records/listByDate";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the specified date");
            return "redirect:/web/records/approved";
        }
    }


    @GetMapping("/categories/{categoryId}")
    public String getRecordsByCategory(@PathVariable Long categoryId, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getRecordsByCategory(categoryId);
            model.addAttribute("records", records);
            model.addAttribute("selectedCategoryId", categoryId);
            return "records/categoryRecords";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the selected category.");
            return "redirect:/web/records/approved";
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
            return "redirect:/web/records/approved";
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
            return "redirect:/web/records/approved";
        }
    }

}
