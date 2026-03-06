package com.group1.cartservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToCartRequestDTO(
        @NotBlank(message = "Product Code is Required")
        String productCode,

        @NotNull(message = "Quantity is Required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {
}
