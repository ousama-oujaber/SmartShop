package com.edu.smartshop.entity;

import com.edu.smartshop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private BigDecimal subTotal;

    private BigDecimal totalDiscount;

    private BigDecimal tax;

    private BigDecimal totalAmount;

    private BigDecimal remainingAmount;

    private String promoCode;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
