package com.edu.smartshop.dto.response;

import com.edu.smartshop.enums.OrderStatus;
import com.edu.smartshop.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String clientName;
    private List<OrderItemResponseDTO> items;
    private BigDecimal subTotal;
    private BigDecimal totalDiscount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
}
