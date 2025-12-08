package com.edu.smartshop.entity;

import com.edu.smartshop.enums.CustomerTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String fullName;

    private String email;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier;

    private BigDecimal totalSpent;

    private Integer totalOrders;

    private LocalDateTime firstOrderDate;

    private LocalDateTime lastOrderDate;
}
