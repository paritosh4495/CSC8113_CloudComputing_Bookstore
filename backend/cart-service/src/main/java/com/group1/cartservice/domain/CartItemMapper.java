package com.group1.cartservice.domain;

import com.group1.cartservice.domain.model.CartItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "price", target = "unitPrice")
    @Mapping(target = "subTotal", expression = "java(cartItemEntity.getPrice().multiply(java.math.BigDecimal.valueOf(cartItemEntity.getQuantity())))")
    CartItemResponseDTO toCartItemResponseDTO(CartItemEntity cartItemEntity);

}
