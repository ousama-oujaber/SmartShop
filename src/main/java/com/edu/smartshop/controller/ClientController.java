package com.edu.smartshop.controller;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.request.ClientUpdateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.service.IClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientCreateDTO createDTO) {
        ClientResponseDTO client = clientService.createClient(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        List<ClientResponseDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateDTO updateDTO) {
        ClientResponseDTO client = clientService.updateClient(id, updateDTO);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDTO>> getClientOrderHistory(@PathVariable Long id) {
        List<OrderResponseDTO> orders = clientService.getClientOrderHistory(id);
        return ResponseEntity.ok(orders);
    }
}

