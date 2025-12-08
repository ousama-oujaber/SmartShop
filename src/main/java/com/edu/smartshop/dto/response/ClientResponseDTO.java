package com.edu.smartshop.dto.response;

import com.edu.smartshop.enums.CustomerTier;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClientResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private CustomerTier tier;
    private BigDecimal totalSpent;
    private Integer totalOrders;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
}
