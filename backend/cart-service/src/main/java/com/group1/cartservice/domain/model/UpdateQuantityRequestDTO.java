package com.group1.cartservice.domain.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateQuantityRequestDTO(
        @Positive
        @NotNull
        Integer quantity
) {
}
