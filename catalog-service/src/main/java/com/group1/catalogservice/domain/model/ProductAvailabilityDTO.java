package com.group1.catalogservice.domain.model;

public record ProductAvailabilityDTO(
    String code,
    boolean available,
    int availableQuantity
) {

}
