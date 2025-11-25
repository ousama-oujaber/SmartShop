package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "clientId", target = "client.id")
    Order toEntity(OrderCreateDTO dto);

    @Mapping(source = "client.user.username", target = "clientName")
    OrderResponseDTO toDto(Order entity);
}
