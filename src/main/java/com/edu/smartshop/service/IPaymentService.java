package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.PaymentCreateDTO;
import com.edu.smartshop.entity.Payment;

import java.util.List;

public interface IPaymentService {
    Payment addPayment(PaymentCreateDTO createDTO);
    Payment getPaymentById(Long paymentId);
    List<Payment> getPaymentsByOrderId(Long orderId);
    Payment encashPayment(Long paymentId);
    Payment rejectPayment(Long paymentId);
}

