package com.group1.catalogservice.domain;

import com.group1.catalogservice.domain.model.*;

import java.math.BigDecimal;

public interface ProductService {

    PageResult<ProductShortResponseDTO> getAllProducts(int pageNo);
    ProductDetailedResponseDTO getProductByCode(String code);
    PageResult<ProductShortResponseDTO> searchProducts(String query, String genre, String author, String name, String isbn, BigDecimal minPrice, BigDecimal maxPrice, int pageNo);

    //

    ProductDetailedResponseDTO createProduct(CreateProductRequestDTO request);
    ProductDetailedResponseDTO updateProduct(String code, UpdateProductRequestDTO request);

    //
    ProductDetailedResponseDTO updateProductPrice(String code, BigDecimal newPrice);
    void deleteProduct(String code);

    //
    ProductAvailabilityDTO checkProductAvailability(String code);

    void reserveStock(String code, int quantity);

    void releaseStock(String code, int quantity);


}
