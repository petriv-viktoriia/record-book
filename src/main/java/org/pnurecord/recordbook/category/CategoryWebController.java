package org.pnurecord.recordbook.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/web/categories")
public class CategoryWebController {
    private final CategoryService categoryService;

    @GetMapping
    public String getAllCategories(Model model) {
        List<CategoryDto> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories/list"; // resources/templates/categories/list.html
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "categories/form"; // resources/templates/categories/form.html
    }

    @PostMapping
    public String createCategory(@ModelAttribute CategoryDto categoryDto) {
        categoryService.createCategory(categoryDto);
        return "redirect:/web/categories";
    }

    @GetMapping("/edit/{categoryId}")
    public String showEditForm(@PathVariable Long categoryId, Model model) {
        CategoryDto category = categoryService.findById(categoryId);
        model.addAttribute("category", category);
        return "categories/form"; // resources/templates/categories/form.html
    }

    @PostMapping("/update/{categoryId}")
    public String updateCategory(@PathVariable Long categoryId, @ModelAttribute CategoryDto categoryDto) {
        categoryService.updateCategory(categoryId, categoryDto);
        return "redirect:/web/categories";
    }

    @DeleteMapping("/delete/{categoryId}")
    public String deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return "redirect:/web/categories";
    }

}
