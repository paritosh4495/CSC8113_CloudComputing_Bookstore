package com.group1.catalogservice.domain.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public static ProductAlreadyExistsException forIsbn(String isbn) {
        return new ProductAlreadyExistsException("Product with isbn '" + isbn + "' already exists.");
    }
}
