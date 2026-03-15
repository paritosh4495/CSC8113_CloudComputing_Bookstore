package com.group1.cartservice.clients.catalog;

import com.group1.cartservice.ApplicationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogServiceClientConfig {

    @Bean
    RestClient restClient(RestClient.Builder builder, ApplicationProperties properties) {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.simple()
                .withCustomizer(customizer -> {
                    customizer.setConnectTimeout(java.time.Duration.ofSeconds(5));
                    customizer.setReadTimeout(java.time.Duration.ofSeconds(5));
                })
                .build();

        return builder
                .baseUrl(properties.catalogServiceUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
