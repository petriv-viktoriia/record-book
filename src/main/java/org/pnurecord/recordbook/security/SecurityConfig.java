package org.pnurecord.recordbook.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/web/records/create",
                                "/web/records/delete/{recordId}",
                                "/web/records/edit/{recordId}",
                                "/web/records/authors/{authorID}",
                                "/web/records/user/{userID}?status={status}"
                        ).hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN")

                        .requestMatchers(
                                "/web/records/approved",
                                "/web/records/{id}",
                                "/web/records/search",
                                "/web/records/date/{date}",
                                "/web/records/categories/{categoryId}"
                        ).permitAll()

                        .requestMatchers(
                                "/web/records/pending",
                                "/web/records/{recordId}/approve",
                                "/web/records/{recordId}/reject"
                        ).hasAuthority("ROLE_ADMIN")

                        .requestMatchers("/", "/login", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )
                .logout(logout -> logout.logoutUrl("/logout"));

        return http.build();
    }
}