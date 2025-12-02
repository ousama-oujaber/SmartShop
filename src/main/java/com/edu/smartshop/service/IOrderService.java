package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;

import java.util.List;

public interface IOrderService {
    OrderResponseDTO createOrder(OrderCreateDTO createDTO);
    OrderResponseDTO getOrderById(Long orderId);
    OrderResponseDTO confirmOrder(Long orderId);
    OrderResponseDTO cancelOrder(Long orderId);
    List<OrderResponseDTO> getAllOrders(Long clientId);
}

