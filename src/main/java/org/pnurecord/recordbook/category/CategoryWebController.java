package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Controller
@RequestMapping("/web/categories")
public class CategoryWebController {
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String getAllCategories(Model model) {
        List<CategoryDto> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("role", userService.getCurrentUserRole());
        return "categories/list"; // resources/templates/categories/list.html
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        model.addAttribute("role", userService.getCurrentUserRole());
        return "categories/form"; // resources/templates/categories/form.html
    }

    @PostMapping
    public String createCategory(@ModelAttribute CategoryDto categoryDto, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(categoryDto);
            return "redirect:/web/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/categories/new";
        }
    }


    @GetMapping("/edit/{categoryId}")
    public String showEditForm(@PathVariable Long categoryId, Model model) {
        CategoryDto category = categoryService.findById(categoryId);
        model.addAttribute("category", category);
        model.addAttribute("role", userService.getCurrentUserRole());
        return "categories/form"; // resources/templates/categories/form.html
    }

    @PostMapping("/update/{categoryId}")
    public String updateCategory(@PathVariable Long categoryId, @ModelAttribute CategoryDto categoryDto, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(categoryId, categoryDto);
            return "redirect:/web/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/categories/edit/" + categoryId;
        }
    }

    @DeleteMapping("/delete/{categoryId}")
    public String deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return "redirect:/web/categories";
    }

}
