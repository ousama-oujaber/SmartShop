package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.PaymentCreateDTO;
import com.edu.smartshop.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "orderId", target = "order.id")
    Payment toEntity(PaymentCreateDTO dto);
}
