package com.group1.cartservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

@Import({TestcontainersConfiguration.class, SecurityTestConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 0))
public class AbstractIT {

    @LocalServerPort
    int port;

    @InjectWireMock
    protected WireMockServer wireMockServer;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("cart.catalog-service-url",() -> "http://localhost:${wiremock.server.port}");

        // 1.  Disable retries so the 503 fallback triggers instantly
        registry.add("resilience4j.retry.instances.catalog-retry.max-attempts", () -> 1);
        registry.add("resilience4j.retry.instances.catalog-retry.wait-duration", () -> "10ms");

        // 2. Make the sliding window massive so it never accidentally OPENS during other tests
        registry.add("resilience4j.circuitbreaker.instances.catalog-cb.sliding-window-size", () -> 100);
        registry.add("resilience4j.circuitbreaker.instances.catalog-cb.minimum-number-of-calls", () -> 100);

        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost/test-only-placeholder");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        wireMockServer.resetAll();
    }

    protected String getToken(String username) {
        return SecurityTestConfig.generateToken(username);
    }
    // Mock Get Product By Code Check

    protected void mockGetProductByCode(String code, String name, BigDecimal price) {
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/products/" + code))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                        """
                                {
                                    "code": "%s",
                                    "name": "%s",
                                    "price": %f
                                }
                        """.formatted(code,name,price.doubleValue()))));

    }

    // Mock the Availability of Product Check

    protected void mockCheckAvailability(String code, int availableQuantity) {
        wireMockServer.stubFor(WireMock.get(urlMatching("/api/products/" + code + "/availability"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(
                                """
                                        {
                                            "code": "%s",
                                            "availableQuantity": %d
                                        }
                                """.formatted(code, availableQuantity))));
    }

    // Mock a Successful Reservation
    protected void mockReserveStockSuccess(String code) {
        wireMockServer.stubFor(WireMock.post(urlMatching("/api/products/" + code + "/reserve"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    protected void mockReleaseStockSuccess(String code) {
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/api/products/" + code + "/release"))
                .willReturn(WireMock.aResponse().withStatus(200)));
    }

}
