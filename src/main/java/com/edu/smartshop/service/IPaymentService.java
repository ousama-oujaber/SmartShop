package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.PaymentCreateDTO;
import com.edu.smartshop.entity.Payment;

public interface IPaymentService {
    Payment addPayment(PaymentCreateDTO createDTO);
    Payment getPaymentById(Long paymentId);
}
