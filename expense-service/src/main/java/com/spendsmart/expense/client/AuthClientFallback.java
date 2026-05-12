package com.spendsmart.expense.client;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthClientFallback implements AuthClient {

    @Override
    public Map<String, Object> getSubscriptionStatus(int userId) {
        // Graceful fallback to avoid blocking expense creation when auth-service is unavailable.
        log.warn("AuthClient fallback triggered for userId={}. Returning PREMIUM fallback subscription.", userId);
        return Map.of("subscriptionType", "PREMIUM", "fallback", true);
    }
}
