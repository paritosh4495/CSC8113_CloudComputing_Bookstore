package com.group1.cartservice.domain;

import com.group1.cartservice.domain.model.AddToCartRequestDTO;
import com.group1.cartservice.domain.model.CartResponseDTO;
import com.group1.cartservice.domain.model.UpdateQuantityRequestDTO;

public interface CartService {



    CartResponseDTO getCartByUserId(String userId);
    CartResponseDTO getCartById(Long cartId);

    CartResponseDTO addItemToCart(String userId, AddToCartRequestDTO request);
    CartResponseDTO updateItemQuantity(String userId, Long cartItemId, UpdateQuantityRequestDTO request);


     void removeCartItem(String userId, Long cartItemId);
     void clearCart(String userId);

}
