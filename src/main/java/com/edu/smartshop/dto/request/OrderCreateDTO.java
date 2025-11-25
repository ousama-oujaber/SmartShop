package com.edu.smartshop.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
