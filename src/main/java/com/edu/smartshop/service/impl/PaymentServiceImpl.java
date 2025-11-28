package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.PaymentCreateDTO;
import com.edu.smartshop.entity.Order;
import com.edu.smartshop.entity.Payment;
import com.edu.smartshop.enums.PaymentMethod;
import com.edu.smartshop.enums.PaymentStatus;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.repository.OrderRepository;
import com.edu.smartshop.repository.PaymentRepository;
import com.edu.smartshop.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class    PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private static final BigDecimal CASH_LIMIT = new BigDecimal("20000");

    @Override
    public Payment addPayment(PaymentCreateDTO createDTO) {
        log.info("Processing payment for order ID: {}, Amount: {}, Method: {}",
                createDTO.getOrderId(), createDTO.getAmount(), createDTO.getPaymentMethod());

        Order order = orderRepository.findById(createDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + createDTO.getOrderId()));

        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessRuleException("Order is already fully paid");
        }

        if (createDTO.getAmount().compareTo(order.getRemainingAmount()) > 0) {
            throw new BusinessRuleException(
                    String.format("Payment amount (%.2f DH) exceeds remaining amount (%.2f DH)",
                            createDTO.getAmount(), order.getRemainingAmount()));
        }

        if (createDTO.getPaymentMethod() == PaymentMethod.ESPECES &&
                createDTO.getAmount().compareTo(CASH_LIMIT) > 0) {
            throw new BusinessRuleException(
                    String.format("Cash payments cannot exceed %.2f DH. Amount: %.2f DH",
                            CASH_LIMIT, createDTO.getAmount()));
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(createDTO.getAmount())
                .paymentMethod(createDTO.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.EN_ATTENTE)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newRemainingAmount = order.getRemainingAmount().subtract(createDTO.getAmount());
        order.setRemainingAmount(newRemainingAmount);
        orderRepository.save(order);

        log.info("Payment created successfully. Payment ID: {}, Order remaining: {} DH",
                savedPayment.getId(), newRemainingAmount);

        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            log.info("Order {} is now fully paid!", order.getId());
        }

        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }
}
