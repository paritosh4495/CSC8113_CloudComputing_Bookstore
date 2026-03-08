package com.group1.catalogservice.domain;

import com.group1.catalogservice.ApplicationProperties;
import com.group1.catalogservice.domain.exceptions.InsufficientStockException;
import com.group1.catalogservice.domain.exceptions.ProductAlreadyExistsException;
import com.group1.catalogservice.domain.exceptions.ProductNotFoundException;
import com.group1.catalogservice.domain.mapper.ProductMapper;
import com.group1.catalogservice.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ProductServiceImpl implements ProductService {

    private final ApplicationProperties applicationProperties;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override

    public PageResult<ProductShortResponseDTO> getAllProducts(int pageNo) {
        Sort sort = Sort.by("name").ascending();
        pageNo = pageNo <= 1 ? 0 : pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, applicationProperties.pageSize(), sort);
        Page<ProductShortResponseDTO> productPage = productRepository.findByStatusNot(Status.DISCONTINUED,pageable)
                .map(productMapper::toProductShortResponseDTO);

        return new PageResult<>(
                productPage.getContent(),
                (int) productPage.getTotalElements(),
                productPage.getNumber() + 1,
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    @Override
    public ProductDetailedResponseDTO getProductByCode(String code) {
        ProductDetailedResponseDTO productResponse =  productRepository.findByCodeAndStatusNot(code, Status.DISCONTINUED)
                .map(productMapper::toProductDetailedResponseDTO)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));

        boolean isAvailable = productResponse.stockQuantity() > 0 && productResponse.status() == Status.AVAILABLE;
        return  productResponse.withAvailable(isAvailable);
    }

    @Override
    public PageResult<ProductShortResponseDTO> searchProducts(String query, String genre, String author, String name, String isbn, BigDecimal minPrice, BigDecimal maxPrice, int pageNo) {
        Sort sort = Sort.by("name").ascending();
        pageNo = pageNo <= 1 ? 0 : pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, applicationProperties.pageSize(), sort);
        Page<ProductShortResponseDTO> productPage = productRepository.findAll(
                        ProductSpecification.search(query, genre, author, name, isbn, minPrice, maxPrice),
                        pageable
        )
                .map(productMapper::toProductShortResponseDTO);

        return new PageResult<>(
                productPage.getContent(),
                (int) productPage.getTotalElements(),
                productPage.getNumber() + 1,
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    @Override
    @Transactional
    public ProductDetailedResponseDTO createProduct(CreateProductRequestDTO request) {
        // Check if product with same isbn already exists
        if (productRepository.existsByIsbn(request.isbn())) {
            throw ProductAlreadyExistsException.forIsbn(request.isbn());
        }
        // Create ProductEntity from request
        ProductEntity productEntity = productMapper.toProductEntity(request);
        productEntity.setCode(UUID.randomUUID().toString());
        // Save to repository
        ProductEntity savedEntity = productRepository.save(productEntity);
        // Convert to response DTO and return
        return productMapper.toProductDetailedResponseDTO(savedEntity);
    }

    @Override
    @Transactional
    public ProductDetailedResponseDTO updateProduct(String code, UpdateProductRequestDTO request) {
        ProductEntity existingProduct = productRepository.findByCode(code)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));

        // Update fields
        productMapper.updateProductEntityFromRequestDTO(request, existingProduct);
        // Save updated entity
        ProductEntity updatedEntity = productRepository.save(existingProduct);
        // Convert to response DTO and return
        return productMapper.toProductDetailedResponseDTO(updatedEntity);
    }

    @Override
    @Transactional
    public ProductDetailedResponseDTO updateProductPrice(String code, BigDecimal newPrice) {
        ProductEntity existingProduct = productRepository.findByCode(code)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));

        existingProduct.setPrice(newPrice);
        ProductEntity updatedEntity = productRepository.save(existingProduct);
        return productMapper.toProductDetailedResponseDTO(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteProduct(String code) {
        ProductEntity product = productRepository.findByCode(code)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));
        product.setStatus(Status.DISCONTINUED);
        productRepository.save(product);
    }

    @Override
    public ProductAvailabilityDTO checkProductAvailability(String code) {
        ProductEntity product = productRepository.findByCodeAndStatusNot(
                code, Status.DISCONTINUED
        ).orElseThrow(() -> ProductNotFoundException.forCode(code));

        boolean isAvailable = product.getStockQuantity() > 0 && product.getStatus() == Status.AVAILABLE;
        return new ProductAvailabilityDTO( product.getCode(),isAvailable, product.getStockQuantity());
    }


    @Override
    @Transactional
    public void reserveStock(String code, int quantity) {
        ProductEntity product = productRepository.findByCodeAndStatusNot(
                code, Status.DISCONTINUED
        ).orElseThrow(() -> ProductNotFoundException.forCode(code));

        if (product.getStockQuantity() < quantity) {
            throw InsufficientStockException.forProduct(code, quantity, product.getStockQuantity());
        }
        int diff = product.getStockQuantity() - quantity;
        if(diff == 0){
            product.setStatus(Status.OUT_OF_STOCK);
        }
        product.setStockQuantity(diff);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void releaseStock(String code, int quantity) {
        ProductEntity product = productRepository.findByCodeAndStatusNot(
                code, Status.DISCONTINUED
        ).orElseThrow(() -> ProductNotFoundException.forCode(code));

        int newStock = product.getStockQuantity() + quantity;
        product.setStockQuantity(newStock);
        if(product.getStatus() == Status.OUT_OF_STOCK && newStock > 0){
            product.setStatus(Status.AVAILABLE);
        }
        productRepository.save(product);
    }
}
