package com.edu.smartshop.controller;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        OrderResponseDTO order = orderService.createOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false) Long clientId) {
        List<OrderResponseDTO> orders = orderService.getAllOrders(clientId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<OrderResponseDTO> validateOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }
}

