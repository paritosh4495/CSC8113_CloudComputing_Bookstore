package com.group1.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Tie in our Keycloak role extractor
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // --- PUBLIC: anyone can browse the catalog ---
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // --- INTERNAL: only called by cart-service, lock down to authenticated ---
                        // reserve and release stock should not be publicly accessible
                        .requestMatchers(HttpMethod.POST, "/api/products/*/reserve").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/release").authenticated()

                        // --- ADMIN ONLY: product management ---
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("admin")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("admin")

                        // Swagger / actuator open
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }
}
