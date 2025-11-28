package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.enums.CustomerTier;

public interface IClientService {
    ClientResponseDTO createClient(ClientCreateDTO createDTO);
    ClientResponseDTO getClientById(Long clientId);
    CustomerTier calculateTier(Client client);
    void updateClientTier(Client client);
}
