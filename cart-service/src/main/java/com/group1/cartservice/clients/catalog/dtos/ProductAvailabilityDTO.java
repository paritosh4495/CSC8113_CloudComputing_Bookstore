package com.group1.cartservice.clients.catalog.dtos;

public record ProductAvailabilityDTO(
    String code,
    boolean available,
    int availableQuantity
) {

}
