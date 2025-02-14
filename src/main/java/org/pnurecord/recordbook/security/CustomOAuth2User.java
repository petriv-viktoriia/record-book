package org.pnurecord.recordbook.security;

import lombok.Getter;
import org.pnurecord.recordbook.user.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    private final Role role;

    public CustomOAuth2User(OAuth2User oAuth2User, Role role) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)), oAuth2User.getAttributes(), "email");
        this.role = role;
    }
}