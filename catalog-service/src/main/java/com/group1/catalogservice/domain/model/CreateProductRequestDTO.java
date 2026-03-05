package com.group1.catalogservice.domain.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequestDTO(

        @NotBlank(message = "Product name is required")
        String name,
        @NotBlank(message = "Author name is required")
        String author,
        String isbn,
        String description,
        String imageUrl,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        BigDecimal price,
        String genre,
        String publisher,
        @Min(value = 100, message = "Invalid publication year")
        Integer publicationYear,
        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,
        @NotNull(message = "Status is required")
        Status status
) {
}

