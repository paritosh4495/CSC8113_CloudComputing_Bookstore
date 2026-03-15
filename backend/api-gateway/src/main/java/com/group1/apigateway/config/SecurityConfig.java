package com.group1.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .pathMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/static/**",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.png",
                                "/*.svg",
                                "/*.webp",
                                "/*.woff",
                                "/*.woff2"
                        ).permitAll()


                        .pathMatchers(HttpMethod.GET, "/catalog/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/healthz").permitAll()
                        .pathMatchers("/admin/**").permitAll()          // Keycloak admin console
                        .pathMatchers("/realms/**").permitAll()         // Keycloak endpoints
                        .pathMatchers("/resources/**").permitAll()      // Keycloak static assets
                        .pathMatchers("/js/**").permitAll()             // Keycloak JS




                        .pathMatchers(HttpMethod.POST, "/catalog/*/reserve").authenticated()
                        .pathMatchers(HttpMethod.POST, "/catalog/*/release").authenticated()


                        .pathMatchers(HttpMethod.POST, "/catalog/**").hasRole("admin")
                        .pathMatchers(HttpMethod.PUT, "/catalog/**").hasRole("admin")
                        .pathMatchers(HttpMethod.PATCH, "/catalog/**").hasRole("admin")
                        .pathMatchers(HttpMethod.DELETE, "/catalog/**").hasRole("admin")


                        .pathMatchers("/cart/**").authenticated()



                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
