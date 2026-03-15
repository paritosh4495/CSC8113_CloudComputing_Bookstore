package com.group1.cartservice.domain;

import com.group1.cartservice.domain.model.CartResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {



    @Mapping(target = "totalItems", expression = "java(cartEntity.getItems() != null ? cartEntity.getItems().stream().mapToInt(item -> item.getQuantity()).sum() : 0)")
    @Mapping(target = "totalPrice", expression = "java(cartEntity.getItems() != null ? cartEntity.getItems().stream().map(item -> item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add) : java.math.BigDecimal.ZERO)")
    CartResponseDTO toCartResponseDTO(CartEntity cartEntity);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    @Mapping(target = "version", ignore = true)
    CartEntity toCartEntity(String userId);
}

