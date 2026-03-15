package com.group1.cartservice.domain.exceptions;

public class InvalidCartOperationException extends RuntimeException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
