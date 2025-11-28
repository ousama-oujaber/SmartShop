package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.request.OrderItemDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.entity.Order;
import com.edu.smartshop.entity.OrderItem;
import com.edu.smartshop.entity.Product;
import com.edu.smartshop.enums.CustomerTier;
import com.edu.smartshop.enums.OrderStatus;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.mapper.OrderMapper;
import com.edu.smartshop.repository.ClientRepository;
import com.edu.smartshop.repository.OrderItemRepository;
import com.edu.smartshop.repository.OrderRepository;
import com.edu.smartshop.repository.ProductRepository;
import com.edu.smartshop.service.IClientService;
import com.edu.smartshop.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final IClientService clientService;
    private final OrderMapper orderMapper;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");
    private static final BigDecimal PROMO_DISCOUNT = new BigDecimal("0.05");
    private static final String PROMO_CODE_PREFIX = "PROMO-";

    @Override
    public OrderResponseDTO createOrder(OrderCreateDTO createDTO) {
        log.info("Creating order for client ID: {}", createDTO.getClientId());

        Client client = clientRepository.findById(createDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + createDTO.getClientId()));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : createDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

            if (product.isDeleted()) {
                throw new BusinessRuleException("Product '" + product.getName() + "' is not available");
            }

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessRuleException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), itemDTO.getQuantity()));
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(new BigDecimal(itemDTO.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            subTotal = subTotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice())
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(orderItem);

            log.debug("Added order item: Product={}, Qty={}, Price={}, LineTotal={}",
                    product.getName(), itemDTO.getQuantity(), product.getPrice(), lineTotal);
        }

        CustomerTier tier = clientService.calculateTier(client);
        BigDecimal tierDiscountRate = getTierDiscountRate(tier, subTotal);
        BigDecimal tierDiscount = subTotal.multiply(tierDiscountRate).setScale(2, RoundingMode.HALF_UP);

        log.debug("Client tier: {}, SubTotal: {}, Discount rate: {}%, Tier discount: {}",
                tier, subTotal, tierDiscountRate.multiply(new BigDecimal("100")), tierDiscount);

        BigDecimal promoDiscount = BigDecimal.ZERO;
        if (createDTO.getPromoCode() != null && !createDTO.getPromoCode().trim().isEmpty()) {
            if (isValidPromoCode(createDTO.getPromoCode())) {
                promoDiscount = subTotal.multiply(PROMO_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
                log.info("Valid promo code '{}' applied. Discount: {}", createDTO.getPromoCode(), promoDiscount);
            } else {
                log.warn("Invalid promo code provided: {}", createDTO.getPromoCode());
                throw new BusinessRuleException("Invalid promo code: " + createDTO.getPromoCode());
            }
        }

        BigDecimal totalDiscount = tierDiscount.add(promoDiscount);
        BigDecimal netCommercial = subTotal.subtract(totalDiscount);
        BigDecimal tax = netCommercial.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTTC = netCommercial.add(tax);

        log.debug("Order calculations - SubTotal: {}, TierDiscount: {}, PromoDiscount: {}, TotalDiscount: {}, NetCommercial: {}, Tax: {}, TotalTTC: {}",
                subTotal, tierDiscount, promoDiscount, totalDiscount, netCommercial, tax, totalTTC);

        Order order = Order.builder()
                .client(client)
                .date(LocalDateTime.now())
                .subTotal(subTotal)
                .totalDiscount(totalDiscount)
                .tax(tax)
                .totalAmount(totalTTC)
                .remainingAmount(totalTTC)
                .promoCode(createDTO.getPromoCode())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        log.info("Order created successfully. Order ID: {}, Total: {}, Status: {}", savedOrder.getId(), totalTTC, OrderStatus.PENDING);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDto(order);
    }

    @Override
    public OrderResponseDTO confirmOrder(Long orderId) {
        log.info("Confirming order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Order is already confirmed");
        }

        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException(
                    String.format("Cannot confirm unpaid order. Remaining amount: %.2f DH",
                            order.getRemainingAmount()));
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            Product product = item.getProduct();

            if (product.getStock() < item.getQuantity()) {
                throw new BusinessRuleException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Required: %d",
                                product.getName(), product.getStock(), item.getQuantity()));
            }

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            log.debug("Reduced stock for product '{}': {} units", product.getName(), item.getQuantity());
        }

        Client client = order.getClient();
        client.setTotalOrders((client.getTotalOrders() != null ? client.getTotalOrders() : 0) + 1);
        client.setTotalSpent((client.getTotalSpent() != null ? client.getTotalSpent() : BigDecimal.ZERO)
                .add(order.getTotalAmount()));

        clientService.updateClientTier(client);
        clientRepository.save(client);

        log.info("Updated client {} stats: Total Orders: {}, Total Spent: {}, Tier: {}",
                client.getId(), client.getTotalOrders(), client.getTotalSpent(), client.getTier());

        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);

        log.info("Order {} confirmed successfully", orderId);

        return orderMapper.toDto(confirmedOrder);
    }

    private BigDecimal getTierDiscountRate(CustomerTier tier, BigDecimal subTotal) {
        return switch (tier) {
            case PLATINUM -> subTotal.compareTo(new BigDecimal("1200")) >= 0
                    ? new BigDecimal("0.15") : BigDecimal.ZERO;
            case GOLD -> subTotal.compareTo(new BigDecimal("800")) >= 0
                    ? new BigDecimal("0.10") : BigDecimal.ZERO;
            case SILVER -> subTotal.compareTo(new BigDecimal("500")) >= 0
                    ? new BigDecimal("0.05") : BigDecimal.ZERO;
            case BASIC -> BigDecimal.ZERO;
        };
    }

    private boolean isValidPromoCode(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return false;
        }
        return promoCode.toUpperCase().startsWith(PROMO_CODE_PREFIX);
    }
}
