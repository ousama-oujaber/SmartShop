package com.edu.smartshop.service;

import com.edu.smartshop.entity.Client;
import com.edu.smartshop.enums.CustomerTier;

public interface IClientService {
    
    /**
     * Calculate and return the appropriate customer tier based on order count and total spending.
     * 
     * Rules:
     * - PLATINUM: >20 orders OR >15000 DH
     * - GOLD: >10 orders OR >5000 DH
     * - SILVER: >3 orders OR >1000 DH
     * - BASIC: Default (otherwise)
     * 
     * @param client the client to calculate tier for
     * @return the calculated customer tier
     */
    CustomerTier calculateTier(Client client);
    
    /**
     * Update the client's tier based on current statistics.
     * 
     * @param client the client to update
     */
    void updateClientTier(Client client);
}
