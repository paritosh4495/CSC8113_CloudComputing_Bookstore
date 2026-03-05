package com.group1.catalogservice.domain.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;


import java.math.BigDecimal;

public record UpdateProductRequestDTO(
        String name,
        String author,
        String isbn,
        String description,
        String imageUrl,
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        BigDecimal price,
        String genre,
        String publisher,
        @Min(value = 100, message = "Invalid publication year")
        Integer publicationYear,
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,
        Status status
) {
}
