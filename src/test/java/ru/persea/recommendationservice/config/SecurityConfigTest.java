package ru.persea.recommendationservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtAuthenticationConverter_shouldExtractRoles() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user")
                .claim("realm_access", Map.of("roles", List.of("admin", "user")))
                .build();

        var auth = converter.convert(jwt);
        var authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // ROLE_ADMIN и ROLE_USER должны присутствовать (FACTOR_BEARER не мешает)
        assertThat(authorities).contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void jwtAuthenticationConverter_shouldReturnEmptyWhenNoRealmAccess() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user")
                .claim("some_other", "value")
                .build();

        var auth = converter.convert(jwt);
        var authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Никаких ролей из realm_access быть не должно (FACTOR_BEARER игнорируем)
        assertThat(authorities).doesNotContain("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_APP_USER");
    }

    @Test
    void jwtAuthenticationConverter_shouldReturnEmptyWhenRolesIsNull() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user")
                .claim("realm_access", Map.of()) // roles отсутствует
                .build();

        var auth = converter.convert(jwt);
        var authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).doesNotContain("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_APP_USER");
    }

    @Test
    void jwtAuthenticationConverter_shouldReturnEmptyWhenRolesIsEmptyList() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user")
                .claim("realm_access", Map.of("roles", List.of()))
                .build();

        var auth = converter.convert(jwt);
        var authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).doesNotContain("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_APP_USER");
    }
}