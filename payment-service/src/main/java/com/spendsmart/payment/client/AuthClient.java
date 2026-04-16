package com.spendsmart.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

// ============================================================================
// FEIGN CLIENT — Calls auth-service to upgrade user subscription after payment.
//
// After Razorpay payment is verified, we call this to set the user's
// subscriptionType to PREMIUM and premiumExpiresAt to 30 days from now.
//
// WHY url FALLBACK?
//   @FeignClient(name="auth-service") alone relies entirely on Eureka.
//   If Eureka is slow to start or auth-service hasn't registered yet, Feign
//   throws "No instances available for auth-service" → payment verify fails.
//   Setting url = AUTH_SERVICE_URL lets Feign resolve the target directly
//   (localhost:8081 in dev) while still using the Eureka name as identifier.
// ============================================================================
@FeignClient(name = "auth-service", url = "${AUTH_SERVICE_URL:http://localhost:8081}")
public interface AuthClient {

	// Upgrades user to PREMIUM subscription (30 days from now).
	// Called by PaymentServiceImpl after successful payment verification.
	// Security: /auth/subscription/** is permitAll() in auth-service SecurityConfig,
	// so no JWT is required for this internal service-to-service call.
	@PutMapping("/auth/subscription/{userId}/upgrade")
	void upgradeToPremium(@PathVariable("userId") int userId);
}
