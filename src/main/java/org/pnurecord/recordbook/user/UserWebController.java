package org.pnurecord.recordbook.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class UserWebController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @GetMapping
    public String login(){
        return "user/login";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/web/users")
    public String getUsers(Model model){
        List<UserDto> users = userService.getUsers();
        model.addAttribute("users", users);
        model.addAttribute("role", userService.getCurrentUserRole());
        return "user/list";
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
    public String createUser(@Valid UserCreateDto userCreateDto, Model model) {
        model.addAttribute("user", userService.createUser(userCreateDto));
        return "redirect:/web/users";
    }


    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserCreateDto());
        return "user/form";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid UserCreateDto userCreateDto, Model model) {
        model.addAttribute("user", userService.createUser(userCreateDto));
        return "redirect:http://localhost:8080";
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
    public String updateUser(@PathVariable Long id, @ModelAttribute @Valid UserUpdateDto userUpdateDto, Model model) {
        userService.updateUser(id, userUpdateDto);
        return "redirect:/web/users";
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/web/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/web/users";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/web/users/{id}/role")
    public String setUserRole(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("User with id " + id + " not found")
        );

        if (user.getRole().equals(Role.STUDENT)){
            userService.setUserRole(id, Role.ADMIN);
        }

        return "redirect:/web/users";
    }


}
