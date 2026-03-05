package com.group1.catalogservice.web.controllers;


import com.group1.catalogservice.domain.ProductService;
import com.group1.catalogservice.domain.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<PageResult<ProductShortResponseDTO>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "1") int pageNo
    )
    {
        PageResult<ProductShortResponseDTO> products = productService.getAllProducts(pageNo);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{code}")
    public  ResponseEntity<ProductDetailedResponseDTO> getProductByCode(
            @PathVariable String code
    ){
        ProductDetailedResponseDTO product = productService.getProductByCode(code);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/search")
    public ResponseEntity<PageResult<ProductShortResponseDTO>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false)BigDecimal minPrice,
            @RequestParam(required = false)BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "1") int pageNo)
    {
        PageResult<ProductShortResponseDTO> products = productService.searchProducts(
                query, genre, author, name, isbn, minPrice, maxPrice, pageNo
        );
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductDetailedResponseDTO> createProduct(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateProductRequestDTO requestDTO
            ){
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductDetailedResponseDTO createdProduct = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{code}")
    public ResponseEntity<ProductDetailedResponseDTO> updateProduct(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String code,
            @Valid @RequestBody UpdateProductRequestDTO requestDTO
    ) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductDetailedResponseDTO updatedProduct = productService.updateProduct(code, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }


    @PatchMapping("/{code}/price")
    public ResponseEntity<ProductDetailedResponseDTO> updateProductPrice(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String code,
            @RequestParam BigDecimal newPrice
    ) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductDetailedResponseDTO updatedProduct = productService.updateProductPrice(code, newPrice);
        return ResponseEntity.ok(updatedProduct);
    }


    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String code
    ) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.deleteProduct(code);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{code}/availability")
    public ResponseEntity<ProductAvailabilityDTO> checkProductAvailability(
            @PathVariable String code
    ) {
        ProductAvailabilityDTO availability = productService.checkProductAvailability(code);
        return ResponseEntity.ok(availability);
    }


}
