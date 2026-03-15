package com.group1.cartservice.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDTO(
        Long id,
        String userId,
        CartStatus status,
        List<CartItemResponseDTO> items,
        BigDecimal totalPrice,
        Integer totalItems
) {
}
