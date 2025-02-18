package org.pnurecord.recordbook.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.category.CategoryRepository;
import org.pnurecord.recordbook.category.CategoryService;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.reaction.ReactionCountDto;
import org.pnurecord.recordbook.reaction.ReactionDto;
import org.pnurecord.recordbook.reaction.ReactionService;
import org.pnurecord.recordbook.recordFile.RecordFile;
import org.pnurecord.recordbook.recordFile.RecordFileInfoDto;
import org.pnurecord.recordbook.recordFile.RecordFileRepository;
import org.pnurecord.recordbook.recordFile.RecordFileService;
import org.pnurecord.recordbook.user.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        records.sort(Comparator.comparing(RecordDto::getPublishedDate).reversed());

        model.addAttribute("reactions", reactions);
        model.addAttribute("records", records);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("role", userService.getCurrentUserRole());

        return "records/listApproved2";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recordDto", new RecordDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("role", userService.getCurrentUserRole());
        model.addAttribute("files", new ArrayList<>());
        return "records/form2";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @PostMapping("/create")
    public String createRecord(@ModelAttribute @Valid RecordDto recordDto,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) List<MultipartFile> files,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("role", userService.getCurrentUserRole());
            return "records/form2";
        }

        try {

            Long currentUserId = userService.getCurrentUserId();
            recordDto.setAuthorId(currentUserId);
            RecordDto createdRecord = recordService.createRecord(recordDto);

            if (files != null && !files.isEmpty()) {
                recordFileService.saveRecordFile(files, createdRecord.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Record created and file uploaded successfully.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Record created successfully.");
            }
            return "redirect:/web/records/" + createdRecord.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create record: " + e.getMessage());
            return "redirect:/web/records/create";
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/edit/{recordId}")
    public String showEditForm(@PathVariable Long recordId, Model model) {
        RecordDto recordDto = recordService.findById(recordId);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(recordId, baseUrl);


        model.addAttribute("recordDto", recordDto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("role", userService.getCurrentUserRole());
        model.addAttribute("files", files);
        return "records/form2";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @PutMapping("/edit/{recordId}")
    public String updateRecord(@PathVariable Long recordId,
                               @ModelAttribute @Valid RecordDto recordDto,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) List<MultipartFile> files,
                               @RequestParam(value = "filesToDelete", required = false) List<Long> filesToDelete,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("role", userService.getCurrentUserRole());
            return "records/form2";
        }

        try {
            recordService.updateRecord(recordId, recordDto);

            if (filesToDelete != null) {
                for (Long fileId : filesToDelete) {
                    recordFileService.deleteRecordFileById(fileId);
                }
            }

            if (files != null && !files.isEmpty() && files.get(0).getSize() > 0) {
                //recordFileRepository.deleteByRecordId(recordId);
                recordFileService.saveRecordFile(files, recordId);
                redirectAttributes.addFlashAttribute("successMessage", "Record updated and new file uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Record updated successfully");
            }

            return "redirect:/web/records/" + recordId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update record: " + e.getMessage());
            return "redirect:/web/records/edit/" + recordId;
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @DeleteMapping("/delete/{recordId}")
    public String deleteRecord(@PathVariable Long recordId, RedirectAttributes redirectAttributes) {
        try {
            List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(recordId, "");
            for (RecordFileInfoDto file : files) {
                recordFileService.deleteRecordFileById(file.getId());
            }
            reactionService.deleteReactionsByRecordId(recordId);
            recordService.deleteRecord(recordId);
            redirectAttributes.addFlashAttribute("successMessage", "Record successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/web/records";
    }


    @GetMapping("/{id}")
    public String getRecordDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RecordDto record = recordService.findById(id);
            String categoryName = categoryService.findById(record.getCategoryId()).getName();

            String authorName = "null";

            if (record.getAuthorId() != null) {
                var author = userService.getUserById(record.getAuthorId());
                authorName = author.getFirstName() + " " + author.getLastName();
            }

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .build()
                    .toUriString();

            List<RecordFileInfoDto> files = recordFileService.getFilesByRecordId(id, baseUrl);
            ReactionCountDto reactionsCount = reactionService.getReactionsCount(record.getId());


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String role = "ROLE_ANONYMOUS";
            Long currentUserId = null;

            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                role = userService.getCurrentUserRole();
                currentUserId = userService.getCurrentUserId();
            }


            model.addAttribute("record", record);
            model.addAttribute("reactions", reactionsCount);
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("authorName", authorName);
            model.addAttribute("files", files);
            model.addAttribute("role", role);
            model.addAttribute("currentUserId", currentUserId);

            return "records/details2";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found: " + e.getMessage());
            return "redirect:/web/records";
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/pending")
    public String showPendingRecords(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current user authorities: " + auth.getAuthorities());

        List<RecordDto> records = recordService.findAllPendingRecords();
        Map<Long, String> authorNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (RecordDto record : records) {
            authorNames.put(record.getAuthorId(),
                    userRepository.findUserNameById(record.getAuthorId()));

            categoryNames.put(record.getCategoryId(),
                    categoryRepository.findCategoryNameById(record.getCategoryId()));
        }

        records.sort(Comparator.comparing(RecordDto::getPublishedDate).reversed());

        model.addAttribute("records", records);
        model.addAttribute("authorNames", authorNames);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("role", userService.getCurrentUserRole());

        return "records/listPending2";
    }


    @GetMapping("/search")
    public String searchApprovedRecords(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            List<RecordDto> searchResults = Collections.emptyList();

            if (title != null && title.length() >= 2) {
                searchResults = recordService.findApprovedRecordsByTitle(title, limit);
            }

            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : searchResults) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));

                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String role = "ROLE_ANONYMOUS";
            Long currentUserId = null;

            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                role = userService.getCurrentUserRole();
                currentUserId = userService.getCurrentUserId();
            }

            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("records", searchResults);
            model.addAttribute("searchTitle", title);
            model.addAttribute("searchLimit", limit);
            model.addAttribute("role", role);
            model.addAttribute("reactions", reactions);

            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records: " + e.getMessage());
            return "redirect:/web/records";
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending/search")
    public String searchPendingRecords(
            @RequestParam String title,
            @RequestParam(required = false) Integer limit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> searchResults = Collections.emptyList();

            if (title != null && title.length() >= 2) {
                searchResults = recordService.findPendingRecordsByTitle(title, limit);
            }

            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : searchResults) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));

                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }

            model.addAttribute("records", searchResults);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("records", searchResults);
            model.addAttribute("searchTitle", title);
            model.addAttribute("searchLimit", limit);
            model.addAttribute("role", userService.getCurrentUserRole());
            model.addAttribute("reactions", reactions);

            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records: " + e.getMessage());
            return "redirect:/web/records/pending";
        }
    }

    @PutMapping("/{recordId}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String approveRecord(
            @PathVariable Long recordId,
            RedirectAttributes redirectAttributes) {
        try {
            recordService.approveRecord(recordId);
            redirectAttributes.addFlashAttribute("successMessage", "Record successfully approved");
            return "redirect:/web/records/pending";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve record");
            return "redirect:/web/records/" + recordId;
        }

    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{recordId}/reject")
    public String rejectRecord(
            @PathVariable Long recordId,
            RedirectAttributes redirectAttributes) {
        try {
            recordService.rejectRecord(recordId);
            redirectAttributes.addFlashAttribute("successMessage", "Record successfully rejected");
            return "redirect:/web/records/pending";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject record");
            return "redirect:/web/records/" + recordId;
        }
    }

    @GetMapping("/search/date")
    public String getApprovedRecordsByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }
            List<RecordDto> records = recordService.getApprovedRecordsByDate(date);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));
                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String role = "ROLE_ANONYMOUS";
            Long currentUserId = null;

            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                role = userService.getCurrentUserRole();
                currentUserId = userService.getCurrentUserId();
            }

            model.addAttribute("records", records);
            model.addAttribute("selectedDate", date);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("reactions", reactions);
            model.addAttribute("role", role);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the specified date");
            return "redirect:/web/records";
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending/search/date")
    public String getPendingRecordsByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }

            List<RecordDto> records = recordService.getPendingRecordsByDate(date);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));
                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }

            model.addAttribute("records", records);
            model.addAttribute("selectedDate", date);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("reactions", reactions);
            model.addAttribute("role", userService.getCurrentUserRole());
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch pending records for the specified date");
            return "redirect:/web/records/pending";
        }
    }


    @GetMapping("/search/categories")
    public String getApprovedRecordsByCategory(
            @RequestParam Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getApprovedRecordsByCategory(categoryId);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));
                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String role = "ROLE_ANONYMOUS";
            Long currentUserId = null;

            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                role = userService.getCurrentUserRole();
                currentUserId = userService.getCurrentUserId();
            }

            model.addAttribute("records", records);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("reactions", reactions);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("role", role);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the selected category.");
            return "redirect:/web/records";
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending/search/categories")
    public String getPendingRecordsByCategory(
            @RequestParam Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<RecordDto> records = recordService.getPendingRecordsByCategory(categoryId);
            Map<Long, String> authorNames = new HashMap<>();
            Map<Long, String> categoryNames = new HashMap<>();
            Map<Long, ReactionCountDto> reactions = new HashMap<>();

            for (RecordDto record : records) {
                authorNames.put(record.getAuthorId(),
                        userRepository.findUserNameById(record.getAuthorId()));

                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));

                reactions.put(record.getId(), reactionService.getReactionsCount(record.getId()));
            }
            model.addAttribute("records", records);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("authorNames", authorNames);
            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("role", userService.getCurrentUserRole());
            model.addAttribute("reactions", reactions);
            return "records/searchResults";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for the selected category.");
            return "redirect:/web/records/pending";
        }
    }


    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/status")
    public String getUserRecordsByStatus(
            @RequestParam RecordStatus status,
            Model model, RedirectAttributes redirectAttributes) {

        try {
            Long currentUserId = userService.getCurrentUserId();
            List<RecordDto> records = recordService.getRecordsByUserAndStatus(currentUserId, status);

            Map<Long, String> categoryNames = new HashMap<>();

            for (RecordDto record : records) {
                categoryNames.put(record.getCategoryId(),
                        categoryRepository.findCategoryNameById(record.getCategoryId()));
            }

            if (records == null) {
                records = Collections.emptyList();
            }

            model.addAttribute("categoryNames", categoryNames);
            model.addAttribute("records", records);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statuses", RecordStatus.values());
            model.addAttribute("role", userService.getCurrentUserRole());

            return "records/userRecords";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch records for user: " + e.getMessage());
            return "redirect:/web/records";
        }

    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @PostMapping("/{id}/like")
    public String likeRecord(@PathVariable Long id, @RequestParam Long userId) {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setRecordId(id);
        reactionDto.setUserId(userId);
        reactionDto.setLiked(true);
        reactionService.addOrUpdateReaction(reactionDto);
        return "redirect:/web/records/" + id;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @PostMapping("/{id}/dislike")
    public String dislikeRecord(@PathVariable Long id, @RequestParam Long userId) {
        ReactionDto reactionDto = new ReactionDto();
        reactionDto.setRecordId(id);
        reactionDto.setUserId(userId);
        reactionDto.setLiked(false);
        reactionService.addOrUpdateReaction(reactionDto);
        return "redirect:/web/records/" + id;
    }


}