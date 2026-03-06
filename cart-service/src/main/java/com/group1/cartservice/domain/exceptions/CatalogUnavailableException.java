package com.group1.cartservice.domain.exceptions;

public class CatalogUnavailableException extends RuntimeException {
    public CatalogUnavailableException(String message) {
        super(message);
    }
}
