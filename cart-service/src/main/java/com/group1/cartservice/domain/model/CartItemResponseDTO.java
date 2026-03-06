package com.group1.cartservice.domain.model;

import java.math.BigDecimal;

public record CartItemResponseDTO(
        Long id,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal
) {
}
