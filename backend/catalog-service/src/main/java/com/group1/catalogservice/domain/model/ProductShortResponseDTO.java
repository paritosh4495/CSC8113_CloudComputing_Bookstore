package com.group1.catalogservice.domain.model;

import java.math.BigDecimal;

public record ProductShortResponseDTO(
        String code,
        String name,
        String author,
        BigDecimal price,
        String imageUrl,
        Status status
) {
}
