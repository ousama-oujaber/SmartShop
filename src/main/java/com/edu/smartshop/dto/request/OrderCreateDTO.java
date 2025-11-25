package com.edu.smartshop.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemDTO> items;

    private String promoCode;
}
