package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;

public interface IOrderService {
    OrderResponseDTO createOrder(OrderCreateDTO createDTO);
    OrderResponseDTO getOrderById(Long orderId);
    OrderResponseDTO confirmOrder(Long orderId);
}
