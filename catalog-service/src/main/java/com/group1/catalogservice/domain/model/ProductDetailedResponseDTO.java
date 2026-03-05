package com.group1.catalogservice.domain.model;

import java.math.BigDecimal;
public record ProductDetailedResponseDTO(

 String code,
 String name,
 String author,
 String isbn,
 String description,
 String imageUrl,
 BigDecimal price,
 String genre,
 String publisher,
 Integer publicationYear,
 Integer stockQuantity,
 Status status,
 Boolean available
) {

    public ProductDetailedResponseDTO withAvailable(Boolean available) {
        return new ProductDetailedResponseDTO(
            this.code,
            this.name,
            this.author,
            this.isbn,
            this.description,
            this.imageUrl,
            this.price,
            this.genre,
            this.publisher,
            this.publicationYear,
            this.stockQuantity,
            this.status,
            available
        );
    }
}
