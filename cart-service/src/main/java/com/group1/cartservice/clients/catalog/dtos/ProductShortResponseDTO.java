package com.group1.cartservice.clients.catalog.dtos;

import java.math.BigDecimal;

public record ProductShortResponseDTO(
        String code,
        String name,
        BigDecimal price,
        Status status
) {
}
