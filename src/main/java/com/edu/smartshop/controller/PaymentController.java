package com.edu.smartshop.controller;

import com.edu.smartshop.dto.request.PaymentCreateDTO;
import com.edu.smartshop.entity.Payment;
import com.edu.smartshop.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> addPayment(@Valid @RequestBody PaymentCreateDTO createDTO) {
        Payment payment = paymentService.addPayment(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
}
