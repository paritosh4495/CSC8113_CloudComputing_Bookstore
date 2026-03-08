package com.group1.cartservice.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Cart API").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your Keycloak JWT token here")
                        ));
    }


    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            openApi.setServers(List.of(new Server().url("http://localhost:9000").description("API Gateway")));

            Paths facadePaths = new Paths();
            openApi.getPaths().forEach((internalPath, pathItem) -> {
                String publicPath = internalPath.replace("/api/carts", "/cart");
                facadePaths.addPathItem(publicPath, pathItem);
            });
            openApi.setPaths(facadePaths);
        };

    }
}
