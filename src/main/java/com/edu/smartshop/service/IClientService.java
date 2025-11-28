package com.edu.smartshop.service;

import com.edu.smartshop.entity.Client;
import com.edu.smartshop.enums.CustomerTier;

public interface IClientService {
    CustomerTier calculateTier(Client client);
    void updateClientTier(Client client);

}
