package com.spendsmart.income.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// ============================================================================
// FEIGN CLIENT — Calls auth-service to check user's subscription status.
//
// FREEMIUM MODEL:
// Before adding any income, we check if the user is FREE or PREMIUM.
// - FREE users: Max 7 transactions per day (on a per-service basis)
// - PREMIUM users: Unlimited transactions
// ============================================================================
@FeignClient(name = "auth-service", fallback = AuthClientFallback.class)
public interface AuthClient {

	@GetMapping("/auth/subscription/{userId}")
	Map<String, Object> getSubscriptionStatus(@PathVariable("userId") int userId);
}
