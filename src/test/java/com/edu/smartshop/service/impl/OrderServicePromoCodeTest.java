package com.edu.smartshop.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OrderServicePromoCodeTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Should validate correct promo code format")
    void shouldValidateCorrectPromoCode() {
        // Valid patterns: PROMO-[4 uppercase alphanumerics]
        assertTrue(orderService.isValidPromoCode("PROMO-ABCD"));
        assertTrue(orderService.isValidPromoCode("PROMO-1234"));
        assertTrue(orderService.isValidPromoCode("PROMO-AB12"));
    }

    @Test
    @DisplayName("Should invalidate incorrect promo code format")
    void shouldInvalidateIncorrectPromoCode() {
        assertFalse(orderService.isValidPromoCode("PROMO-ABCDE")); // Too long
        assertFalse(orderService.isValidPromoCode("PROMO-ABC"));   // Too short
        assertFalse(orderService.isValidPromoCode("PROMO-abcd"));  // Lowercase
        assertFalse(orderService.isValidPromoCode("promo-ABCD"));  // Prefix lowercase
        assertFalse(orderService.isValidPromoCode("INVALID"));     // Wrong prefix
        assertFalse(orderService.isValidPromoCode("PROMO-123!"));  // Special char
    }
}
