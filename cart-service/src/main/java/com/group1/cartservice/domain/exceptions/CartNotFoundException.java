package com.group1.cartservice.domain.exceptions;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);

    }

    public static CartNotFoundException forUserId(String userId) {
        return new CartNotFoundException("Cart not found for user ID: " + userId);
    }
}
