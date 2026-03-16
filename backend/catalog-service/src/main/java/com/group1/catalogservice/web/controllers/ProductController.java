package com.group1.catalogservice.web.controllers;


import com.group1.catalogservice.domain.ProductService;
import com.group1.catalogservice.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @Operation(
            summary = "GREEN DEPLOYMENT : GET all Products",
            description = "Retrieve a paginated list of all products in the catalog. " +
                    "Supports pagination through the 'page' query parameter. " +
                    "Each page contains a fixed number of products (e.g., 20)."
    )
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

            @Valid @RequestBody CreateProductRequestDTO requestDTO
    ){

        ProductDetailedResponseDTO createdProduct = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductDetailedResponseDTO> updateProduct(

            @PathVariable String code,
            @Valid @RequestBody UpdateProductRequestDTO requestDTO
    ) {

        ProductDetailedResponseDTO updatedProduct = productService.updateProduct(code, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }


    @PatchMapping("/{code}/price")
    public ResponseEntity<ProductDetailedResponseDTO> updateProductPrice(

            @PathVariable String code,
            @RequestParam BigDecimal newPrice
    ) {

        ProductDetailedResponseDTO updatedProduct = productService.updateProductPrice(code, newPrice);
        return ResponseEntity.ok(updatedProduct);
    }


    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteProduct(

            @PathVariable String code
    ) {

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

    @PostMapping("/{code}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable String code,
            @Valid @RequestBody StockUpdateRequestDTO request
    ) {
        productService.reserveStock(code, request.quantity());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{code}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable String code,
            @Valid @RequestBody StockUpdateRequestDTO request
    ) {
        productService.releaseStock(code, request.quantity());
        return ResponseEntity.ok().build();
    }

    // Endpoint for visualizing Blue - Green deployment

    @GetMapping("/version")
    public ResponseEntity<Map<String,String>> getVersion() {
        Map<String,String> info = new HashMap<>();
        info.put("version", "v1.0 - BLUE");
        info.put("color", "blue");
        info.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(info);


        // Green deployment

//        Map<String,String> info = new HashMap<>();
//        info.put("version", "v2.0 - GREEN");
//        info.put("color", "GREEN");
//        info.put("timestamp", Instant.now().toString());
//        return ResponseEntity.ok(info);


    }

}
