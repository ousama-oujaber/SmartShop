package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.request.ClientUpdateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.enums.CustomerTier;

import java.util.List;

public interface IClientService {
    ClientResponseDTO createClient(ClientCreateDTO createDTO);
    ClientResponseDTO getClientById(Long clientId);
    List<ClientResponseDTO> getAllClients();
    ClientResponseDTO updateClient(Long clientId, ClientUpdateDTO updateDTO);
    List<OrderResponseDTO> getClientOrderHistory(Long clientId);
    CustomerTier calculateTier(Client client);
    void updateClientTier(Client client);
}

