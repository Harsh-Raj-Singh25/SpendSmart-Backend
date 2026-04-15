package com.spendsmart.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// ============================================================================
// FEIGN CLIENT — Calls auth-service to upgrade user subscription after payment.
//
// After Razorpay payment is verified, we call this to set the user's
// subscriptionType to PREMIUM and premiumExpiresAt to 30 days from now.
// ============================================================================
@FeignClient(name = "auth-service")
public interface AuthClient {

	// Upgrades user to PREMIUM subscription (30 days from now)
	@PutMapping("/auth/subscription/{userId}/upgrade")
	void upgradeToPremium(@PathVariable("userId") int userId);
}
