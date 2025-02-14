package org.pnurecord.recordbook.security;

import lombok.Getter;
import org.pnurecord.recordbook.user.Role;
import org.pnurecord.recordbook.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private final Role role;
    private final User user;

    public CustomOAuth2User(OAuth2User oAuth2User, User user) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())), oAuth2User.getAttributes(), "email");
        this.role = user.getRole();
        this.user = user;
    }
}