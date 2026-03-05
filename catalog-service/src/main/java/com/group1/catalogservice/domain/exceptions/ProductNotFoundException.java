package com.group1.catalogservice.domain.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException forCode(String code) {
        return new ProductNotFoundException("Product with code '" + code + "' not found.");
    }

    public static ProductNotFoundException forId(Long id) {
        return new ProductNotFoundException("Product with id '" + id + "' not found.");
    }
}
