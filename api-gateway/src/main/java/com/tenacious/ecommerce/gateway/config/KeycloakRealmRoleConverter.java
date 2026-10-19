package com.tenacious.ecommerce.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Keycloak puts realm-level roles under the {@code realm_access.roles}
 * claim, not under the {@code scope}/{@code scp} claim that Spring
 * Security's default {@link org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter}
 * looks at. This converter reads that claim and maps each role to a
 * {@code ROLE_*} authority (e.g. {@code ADMIN} -> {@code ROLE_ADMIN}) so
 * {@code hasRole(...)} checks in the security filter chain work as
 * expected.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
