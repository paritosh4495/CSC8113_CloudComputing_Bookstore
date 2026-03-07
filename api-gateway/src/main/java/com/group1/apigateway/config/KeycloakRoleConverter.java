package com.group1.apigateway.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map;

public class KeycloakRoleConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        // Extract roles from the "realm_access" claim
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if(realmAccess==null || realmAccess.isEmpty()){
            return Flux.empty();
        }
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");

        if(roles==null || roles.isEmpty()){
            return Flux.empty();
        }

        return Flux.fromIterable(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
    }
}
