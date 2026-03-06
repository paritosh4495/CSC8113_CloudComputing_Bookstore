package com.group1.cartservice.domain.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public static InsufficientStockException forProduct(String productCode, int requested, int available) {
        String message = String.format("Insufficient stock for product %s: requested %d, available %d",
                productCode, requested, available);
        return new InsufficientStockException(message);
    }
}
