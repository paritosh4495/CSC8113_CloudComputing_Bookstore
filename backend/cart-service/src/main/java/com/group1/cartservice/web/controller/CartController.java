package com.group1.cartservice.web.controller;

import com.group1.cartservice.config.AuthenticationFacade;
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
    private final AuthenticationFacade authenticationFacade;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCartForCurrentUser(
    ) {
        String userId = authenticationFacade.getCurrentUserId();
        CartResponseDTO cartResponseDTO = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartResponseDTO);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @Valid @RequestBody AddToCartRequestDTO request
    ) {
        String userId = authenticationFacade.getCurrentUserId();
        CartResponseDTO cartResponseDTO = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartResponseDTO);

    }


    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(

            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQuantityRequestDTO request
            ) {
        String userId = authenticationFacade.getCurrentUserId();
        CartResponseDTO cartResponseDTO = cartService.updateItemQuantity(
                userId, itemId, request
        );
        return ResponseEntity.ok(cartResponseDTO);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteCartItem(

            @PathVariable Long itemId
    ) {
        String userId = authenticationFacade.getCurrentUserId();
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(

    ) {
        String userId = authenticationFacade.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
