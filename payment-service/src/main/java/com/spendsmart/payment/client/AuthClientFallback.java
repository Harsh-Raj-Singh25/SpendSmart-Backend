package com.spendsmart.payment.client;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthClientFallback implements AuthClient {

    @Override
    public void upgradeToPremium(int userId) {
        log.error("Payment AuthClient fallback triggered for userId={}. Upgrade call did not reach auth-service.", userId);
        throw new RuntimeException(
                "Payment was successful but subscription upgrade is temporarily unavailable. "
                        + "Please retry shortly or contact support with your payment reference.");
    }
}
