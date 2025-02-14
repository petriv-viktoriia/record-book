package org.pnurecord.recordbook.security;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.user.User;
import org.pnurecord.recordbook.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;


    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomOAuth2User)) {
            return "GUEST";
        }

        CustomOAuth2User currentUser = (CustomOAuth2User) authentication.getPrincipal();
        String userEmail = currentUser.getAttribute("email");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений в базі"));

        return user.getRole().name();
    }

}
