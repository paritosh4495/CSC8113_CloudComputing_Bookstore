package com.group1.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain  springSecurityFilterChain(ServerHttpSecurity http) {

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // 1. Anyone can view the catalog
                        .pathMatchers(HttpMethod.GET, "/catalog/**").permitAll()

                        // 2. ONLY Admins can add/edit products
                        .pathMatchers(HttpMethod.POST, "/catalog/**").hasRole("admin")
                        .pathMatchers(HttpMethod.PUT, "/catalog/**").hasRole("admin")
                        .pathMatchers(HttpMethod.DELETE, "/catalog/**").hasRole("admin")

                        // 3. Any logged-in user can access their cart
                        .pathMatchers("/cart/**").authenticated()

                        .pathMatchers(HttpMethod.GET, "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()


                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
                return http.build();
    }
}
