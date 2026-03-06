package com.group1.cartservice.web.controller;

import com.group1.cartservice.domain.CartService;
import com.group1.cartservice.domain.model.AddToCartRequestDTO;
import com.group1.cartservice.domain.model.CartResponseDTO;
import com.group1.cartservice.domain.model.UpdateQuantityRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCartForCurrentUser(
            @RequestHeader ("X-User-Id") String userId
    ) {
        CartResponseDTO cartResponseDTO = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartResponseDTO);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @RequestHeader ("X-User-Id") String userId,
            @Valid @RequestBody AddToCartRequestDTO request
    ) {
        CartResponseDTO cartResponseDTO = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartResponseDTO);

    }


    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(
            @RequestHeader ("X-User-Id") String userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQuantityRequestDTO request
            ) {
        CartResponseDTO cartResponseDTO = cartService.updateItemQuantity(
                userId, itemId, request
        );
        return ResponseEntity.ok(cartResponseDTO);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteCartItem(
            @RequestHeader ("X-User-Id") String userId,
            @PathVariable Long itemId
    ) {
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader ("X-User-Id") String userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
