package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.enums.CustomerTier;
import com.edu.smartshop.enums.UserRole;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.mapper.ClientMapper;
import com.edu.smartshop.repository.ClientRepository;
import com.edu.smartshop.repository.UserRepository;
import com.edu.smartshop.service.IClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientServiceImpl implements IClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;
    private static final BigDecimal SILVER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal GOLD_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("15000");

    private static final int SILVER_ORDERS = 3;
    private static final int GOLD_ORDERS = 10;
    private static final int PLATINUM_ORDERS = 20;

    @Override
    public ClientResponseDTO createClient(ClientCreateDTO createDTO) {
        log.info("Creating new client with email: {}", createDTO.getEmail());

        if (userRepository.existsByUsername(createDTO.getEmail())) {
            throw new BusinessRuleException("Email already in use");
        }

        User user = User.builder()
                .username(createDTO.getEmail())
                .password(createDTO.getPassword())
                .role(UserRole.CLIENT)
                .build();

        User savedUser = userRepository.save(user);

        Client client = Client.builder()
                .user(savedUser)
                .fullName(createDTO.getFullName())
                .email(createDTO.getEmail())
                .tier(CustomerTier.BASIC)
                .totalSpent(BigDecimal.ZERO)
                .totalOrders(0)
                .build();

        Client savedClient = clientRepository.save(client);

        log.info("Client created successfully with ID: {}", savedClient.getId());

        return clientMapper.toDto(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return clientMapper.toDto(client);
    }

    @Override
    public CustomerTier calculateTier(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        int totalOrders = client.getTotalOrders() != null ? client.getTotalOrders() : 0;
        BigDecimal totalSpent = client.getTotalSpent() != null ? client.getTotalSpent() : BigDecimal.ZERO;

        if (totalOrders > PLATINUM_ORDERS || totalSpent.compareTo(PLATINUM_THRESHOLD) > 0) {
            log.debug("Client {} qualifies for PLATINUM tier (orders: {}, spent: {})",
                    client.getId(), totalOrders, totalSpent);
            return CustomerTier.PLATINUM;
        }

        if (totalOrders > GOLD_ORDERS || totalSpent.compareTo(GOLD_THRESHOLD) > 0) {
            log.debug("Client {} qualifies for GOLD tier (orders: {}, spent: {})", 
                    client.getId(), totalOrders, totalSpent);
            return CustomerTier.GOLD;
        }

        if (totalOrders > SILVER_ORDERS || totalSpent.compareTo(SILVER_THRESHOLD) > 0) {
            log.debug("Client {} qualifies for SILVER tier (orders: {}, spent: {})", 
                    client.getId(), totalOrders, totalSpent);
            return CustomerTier.SILVER;
        }

        log.debug("Client {} assigned BASIC tier (orders: {}, spent: {})", 
                client.getId(), totalOrders, totalSpent);
        return CustomerTier.BASIC;
    }

    @Override
    public void updateClientTier(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        CustomerTier newTier = calculateTier(client);
        CustomerTier currentTier = client.getTier();

        if (currentTier != newTier) {
            log.info("Updating client {} tier from {} to {}", 
                    client.getId(), currentTier, newTier);
            client.setTier(newTier);
            clientRepository.save(client);
        } else {
            log.debug("Client {} tier unchanged: {}", client.getId(), currentTier);
        }
    }
}
