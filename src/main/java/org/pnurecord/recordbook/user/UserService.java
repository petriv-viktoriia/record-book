package org.pnurecord.recordbook.user;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.exceptions.UserNotAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getUsers() {
        return userMapper.toUserDtoList(userRepository.findAll());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new NotFoundException("User with id %s not found".formatted(id))
        );

        return userMapper.toUserDto(user);
    }

    public UserDto createUser(UserCreateDto userCreateDto) {
        boolean exists = userRepository.existsByEmail(userCreateDto.getEmail());

        if (exists) {
            throw new DuplicateValueException("User with email %s already exists".formatted(userCreateDto.getEmail()));
        } else {
            User user = userMapper.toUserCreateDto(userCreateDto);
            user.setRole(userCreateDto.getRole());
            userRepository.save(user);
            return userMapper.toUserDto(user);
        }
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User with id %s not found".formatted(id))
        );
        userRepository.delete(user);
    }

    public List<UserDto> getUsersByRole(Role role) {
        return userMapper.toUserDtoList(userRepository.findByRole(role));
    }

    public UserDto setUserRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User with id %s not found".formatted(id))
        );

        user.setRole(role);
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    public void updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User with id %s not found".formatted(id))
        );

        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        userRepository.save(user);
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException("Користувач не авторизований");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("email");
        }

        return null;
    }

    public String getCurrentUserRole() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> u.getRole().name())
                .orElseThrow(() -> new NotFoundException("Роль не знайдено"));
    }


    public Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getId).orElseThrow(() -> new NotFoundException("Користувача не знайдено"));
    }

}
