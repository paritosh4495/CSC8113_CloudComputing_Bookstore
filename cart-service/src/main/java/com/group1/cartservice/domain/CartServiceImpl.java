package com.group1.cartservice.domain;

import com.group1.cartservice.clients.catalog.ProductServiceClient;
import com.group1.cartservice.clients.catalog.dtos.ProductAvailabilityDTO;
import com.group1.cartservice.clients.catalog.dtos.ProductShortResponseDTO;
import com.group1.cartservice.domain.exceptions.CartNotFoundException;
import com.group1.cartservice.domain.exceptions.InsufficientStockException;
import com.group1.cartservice.domain.exceptions.InvalidCartOperationException;
import com.group1.cartservice.domain.model.AddToCartRequestDTO;
import com.group1.cartservice.domain.model.CartResponseDTO;
import com.group1.cartservice.domain.model.CartStatus;
import com.group1.cartservice.domain.model.UpdateQuantityRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductServiceClient productService;



    private CartEntity getOrCreateActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUserId(userId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartResponseDTO getCartByUserId(String userId) {
        CartEntity cartEntity = getOrCreateActiveCart(userId);
        return cartMapper.toCartResponseDTO(cartEntity);
    }

    @Override
    public CartResponseDTO getCartById(Long cartId) {
        CartEntity cartEntity = cartRepository.findById(cartId)
                .orElseThrow(()-> new CartNotFoundException("Cart not found for ID: " + cartId));
        return cartMapper.toCartResponseDTO(cartEntity);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(String userId, AddToCartRequestDTO request) {
        // 1. Get or create Cart
        CartEntity cartEntity = getOrCreateActiveCart(userId);

        // Fetch Product From Catalog Service
        ProductShortResponseDTO productInfo = productService.getProductByCode(request.productCode())
                .orElseThrow(()-> new InvalidCartOperationException("Product not found for product code: " + request.productCode()));
        // Check Availability
        ProductAvailabilityDTO availableStock = productService.checkProductAvailability(request.productCode())
                .orElseThrow(()-> new InvalidCartOperationException("Product availability not found for product code: " + request.productCode()));

        if(availableStock.availableQuantity() < request.quantity()){
            throw new InsufficientStockException("Insufficient stock for product code: " + request.productCode());
        }
        // Reserve
        productService.reserveStock(request.productCode(), request.quantity());

        // Add Item to Cart
        // Find the item if it already exists in the cart, if yes then update the quantity, else add new item
        Optional<CartItemEntity> existingItem = cartEntity.getItems().stream()
                .filter((item)-> item.getProductCode().equals(request.productCode())).findFirst();

        if(existingItem.isPresent()){
            CartItemEntity item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        }
        else {
            // Create new Cart Item
            CartItemEntity newItem = new CartItemEntity();
            newItem.setProductCode(request.productCode());
            newItem.setProductName(productInfo.name());
            newItem.setPrice(productInfo.price());
            newItem.setQuantity(request.quantity());
            newItem.setCart(cartEntity);
            cartEntity.addItem(newItem);
        }
        CartEntity updatedCart = cartRepository.save(cartEntity);
        return cartMapper.toCartResponseDTO(updatedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateItemQuantity(String userId, Long cartItemId, UpdateQuantityRequestDTO request) {
        // 1. Get or create Cart
        CartEntity cartEntity = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(()-> new CartNotFoundException("Active cart not found for user ID: " + userId));
        Optional<CartItemEntity> existingItem =  cartEntity.getItems().stream().filter((item)-> item.getId().equals(cartItemId)).findFirst();
        if(existingItem.isEmpty()){
            throw new CartItemNotFoundException("Cart item not found for ID: " + cartItemId);
        }
        CartItemEntity item = existingItem.get();
        if(request.quantity() <= 0) {
            // Remove Item from Cart
            cartEntity.removeItem(item);
            // Release Reserved Stock
            productService.releaseStock(item.getProductCode(), item.getQuantity());
        }
        else {
            // Check Availability
            ProductAvailabilityDTO availableStock = productService.checkProductAvailability(item.getProductCode())
                    .orElseThrow(()-> new InvalidCartOperationException("Product availability not found for product code: " + item.getProductCode()));

            int quantityDifference = request.quantity() - item.getQuantity();
            if(quantityDifference > 0){
                if(availableStock.availableQuantity() < quantityDifference){
                    throw new InsufficientStockException("Insufficient stock for product code: " + item.getProductCode());
                }
                // Reserve Additional Stock
                productService.reserveStock(item.getProductCode(), quantityDifference);
            }
            else if(quantityDifference < 0){
                // Release Excess Stock
                productService.releaseStock(item.getProductCode(), -quantityDifference);
            }
            item.setQuantity(request.quantity());
        }
        CartEntity updatedCart = cartRepository.save(cartEntity);
        return cartMapper.toCartResponseDTO(updatedCart);
    }

    @Override
    @Transactional
    public void removeCartItem(String userId, Long cartItemId) {
        // 1. Get Existing Cart
        CartEntity cartEntity = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(()-> new CartNotFoundException("Active cart not found for user ID: " + userId));

        // 2. Get Cart Item
        Optional<CartItemEntity> existingItem =  cartEntity.getItems().stream().filter((item)-> item.getId().equals(cartItemId)).findFirst();
        if(existingItem.isEmpty()){
            throw new CartItemNotFoundException("Cart item not found for ID: " + cartItemId);
        }
        CartItemEntity item = existingItem.get();
        // 3. Remove Item from Cart
        cartEntity.removeItem(item);
        // 4. Release Reserved Stock
        productService.releaseStock(item.getProductCode(), item.getQuantity());
        cartRepository.save(cartEntity);

    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        // 1. Get Existing Cart
        CartEntity cartEntity = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(()-> new CartNotFoundException("Active cart not found for user ID: " + userId));
        // 2. Release Reserved Stock for all items in the cart
        cartEntity.getItems().forEach(item -> {
            productService.releaseStock(item.getProductCode(), item.getQuantity());
        });
        // 3. Clear Cart Items
        cartEntity.getItems().clear();
        cartRepository.save(cartEntity);
    }
}
