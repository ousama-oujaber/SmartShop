package com.edu.smartshop.controller;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.service.IClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientCreateDTO createDTO) {
        ClientResponseDTO client = clientService.createClient(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }
}
