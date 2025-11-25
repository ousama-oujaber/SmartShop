package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.OrderItemDTO;
import com.edu.smartshop.dto.response.OrderItemResponseDTO;
import com.edu.smartshop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "productId", target = "product.id")
    OrderItem toEntity(OrderItemDTO dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponseDTO toDto(OrderItem entity);
}
