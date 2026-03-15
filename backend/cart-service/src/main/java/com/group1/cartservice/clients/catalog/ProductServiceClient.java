package com.group1.cartservice.clients.catalog;

import com.group1.cartservice.clients.catalog.dtos.ProductAvailabilityDTO;
import com.group1.cartservice.clients.catalog.dtos.ProductShortResponseDTO;
import com.group1.cartservice.clients.catalog.dtos.StockUpdateRequestDTO;
import com.group1.cartservice.domain.exceptions.CatalogUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;


    @Retry(name = "catalog-retry")
    @CircuitBreaker(name = "catalog-cb", fallbackMethod = "fallbackGetProduct")
    public Optional<ProductShortResponseDTO> getProductByCode(String code){
        log.info("Fetching Product with code: {} from Catalog Service", code);
        try {
            var product = restClient
                    .get()
                    .uri("/api/products/{code}", code)
                    .retrieve()
                    .body(ProductShortResponseDTO.class);
            return Optional.ofNullable(product);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product with code {} not found in Catalog Service", code);
            return Optional.empty();
        }
    }


    @Retry(name = "catalog-retry")
    @CircuitBreaker(name = "catalog-cb", fallbackMethod = "fallbackCheckAvailability")
    public Optional<ProductAvailabilityDTO> checkProductAvailability(String code) {
        log.info("Checking availability for Product with code: {} from Catalog Service", code);
        try {
            var availability = restClient.get()
                    .uri("/api/products/{code}/availability", code)
                    .retrieve()
                    .body(ProductAvailabilityDTO.class);
            return Optional.ofNullable(availability);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cannot check availability. Product {} not found.", code);
            return Optional.empty();
        }
    }

    @Retry(name = "catalog-retry")
    @CircuitBreaker(name = "catalog-cb", fallbackMethod = "fallbackReserveStock")
    public void reserveStock(String code, int quantity) {
        log.info("Reserving stock for Product with code: {} and quantity: {} in Catalog Service", code, quantity);
        restClient.post()
                .uri("/api/products/{code}/reserve", code)
                .body(new StockUpdateRequestDTO(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    @Retry(name = "catalog-retry")
    @CircuitBreaker(name = "catalog-cb", fallbackMethod = "fallbackReleaseStock")
    public void releaseStock(String code, int quantity) {
        log.info("Releasing stock for Product with code: {} and quantity: {} in Catalog Service", code, quantity);
        restClient.post()
                .uri("/api/products/{code}/release", code)
                .body(new StockUpdateRequestDTO(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    public Optional<ProductShortResponseDTO> fallbackGetProduct(String code, Throwable t) {
        log.error("Catalog Service is DOWN. Fallback triggered for product: {}. Reason: {}", code, t.getMessage());
        throw new CatalogUnavailableException("The Catalog Service is temporarily unavailable. Please try adding to cart later.");
    }
    public Optional<ProductAvailabilityDTO> fallbackCheckAvailability(String code, Throwable t) {
        log.error("Catalog Service is DOWN. Fallback AVAILABILITY triggered for product: {}. Reason: {}", code, t.getMessage());
        throw new CatalogUnavailableException("Catalog Service is temporarily unavailable. Cannot check stock.");
    }

    public void fallbackReserveStock(String code, int quantity, Throwable t) {
        log.error("Catalog Service is DOWN. Fallback RESERVE triggered for product: {}. Reason: {}", code, t.getMessage());
        throw new CatalogUnavailableException("Catalog Service is temporarily unavailable. Cannot reserve stock.");
    }

    public void fallbackReleaseStock(String code, int quantity, Throwable t) {
        log.error("Catalog Service is DOWN. Fallback RELEASE triggered for product: {}. Reason: {}", code, t.getMessage());
        throw new CatalogUnavailableException("Catalog Service is temporarily unavailable. Cannot release stock right now.");
    }


    /*

    If a user deletes an item from their cart,
     but the Catalog Service is down,
      do we stop them from deleting the item?
      Usually, no. For this project, we will still throw the exception so you don't end up with data inconsistencies
      (the cart item deleted, but the stock never returned),
       but in a massive real-world system,
       you would put the "release" message into a Kafka queue
       to be processed when the Catalog comes back online.
     */
}
