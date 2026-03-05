package com.group1.catalogservice.domain.mapper;

import com.group1.catalogservice.domain.ProductEntity;
import com.group1.catalogservice.domain.model.CreateProductRequestDTO;
import com.group1.catalogservice.domain.model.ProductDetailedResponseDTO;
import com.group1.catalogservice.domain.model.ProductShortResponseDTO;
import com.group1.catalogservice.domain.model.UpdateProductRequestDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {


    ProductShortResponseDTO toProductShortResponseDTO(ProductEntity productEntity);

    @Mapping(target = "available", ignore = true)
    ProductDetailedResponseDTO toProductDetailedResponseDTO(ProductEntity productEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", ignore = true) // Will be handled in the service layer
    ProductEntity toProductEntity(CreateProductRequestDTO createProductRequestDTO);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", ignore = true) // Code should not be updated
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductEntityFromRequestDTO(UpdateProductRequestDTO updateRequest, @MappingTarget ProductEntity productEntity);

}
