package org.pnurecord.recordbook.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.reaction.ReactionService;
import org.pnurecord.recordbook.record.RecordDto;
import org.pnurecord.recordbook.record.RecordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class UserWebController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RecordService recordService;
    private final ReactionService reactionService;

    @GetMapping
    public String login() {
        return "user/login";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/web/users")
    public String getUsers(Model model) {
        try {
            List<UserDto> users = userService.getUsers();

            users.sort(Comparator.comparing(UserDto::getId));

            model.addAttribute("users", users);
            model.addAttribute("role", userService.getCurrentUserRole());
            model.addAttribute("currentUserId", userService.getCurrentUserId());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading users: " + e.getMessage());
        }
        return "user/list";
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/web/users/profile")
    public String getUser(Model model) {
        try {
            long userId = userService.getCurrentUserId();
            User user = userRepository.findById(userId).orElse(null);
            model.addAttribute("user", user);
            model.addAttribute("role", userService.getCurrentUserRole());
            model.addAttribute("currentUserId", userId);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading user: " + e.getMessage());
        }
        return "user/profile";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/web/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserCreateDto());
        model.addAttribute("role", userService.getCurrentUserRole());
        return "user/form";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/web/users/create")
    public String createUser(@Valid UserCreateDto userCreateDto, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", userService.createUser(userCreateDto));
            redirectAttributes.addFlashAttribute("successMessage", "User successfully created");
            return "redirect:/web/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating a user: " + e.getMessage());
            return "redirect:/web/users/create";
        }
    }


    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserCreateDto());
        return "user/form";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid UserCreateDto userCreateDto, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", userService.createUser(userCreateDto));
            redirectAttributes.addFlashAttribute("successMessage", "User successfully registered");
            return "redirect:http://localhost:8080";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during registration: " + e.getMessage());
            return "redirect:/registration";
        }

    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @GetMapping("/web/users/edit/{id}")
    public String updateUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("User with id " + id + " not found")
        );

        UserUpdateDto userUpdateDto = userMapper.toUserUpdateDto(user);
        model.addAttribute("user", userUpdateDto);
        model.addAttribute("role", userService.getCurrentUserRole());
        return "user/edit";
    }


    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    @PutMapping("/web/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute @Valid UserUpdateDto userUpdateDto,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userUpdateDto);
            redirectAttributes.addFlashAttribute("successMessage", "User successfully updated");

            String role = userService.getCurrentUserRole();
            if (Objects.equals(role, "STUDENT")) {
                return "redirect:/web/users/profile";
            } else if (Objects.equals(role, "ADMIN")) {
                return "redirect:/web/users";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during update: " + e.getMessage());
            return "redirect:/web/users/edit/" + id;
        }
        return "redirect:/web/users/profile";
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/web/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            long currentUserId = userService.getCurrentUserId();
            String role = userService.getCurrentUserRole();

            if (currentUserId == id && role.equals("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete yourself!");
                return "redirect:/web/users";
            }
            recordService.nullifyAuthorReferences(id);
            reactionService.deleteUserReactions(id);
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User successfully deleted");
            return "redirect:/web/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during deleting: " + e.getMessage());
            return "redirect:/web/users"; //check it
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/web/users/{id}/role")
    public String setUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow(
                    () -> new IllegalStateException("User with id " + id + " not found")
            );

            if (user.getRole().equals(Role.STUDENT)) {
                userService.setUserRole(id, Role.ADMIN);
            }

            return "redirect:/web/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during role change: " + e.getMessage());
            return "redirect:/web/users"; //check it
        }
    }


}
