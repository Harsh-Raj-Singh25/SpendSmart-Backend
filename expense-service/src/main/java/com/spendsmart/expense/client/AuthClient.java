package com.spendsmart.expense.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// ============================================================================
// FEIGN CLIENT — Calls auth-service to check user's subscription status.
//
// FREEMIUM MODEL:
// Before adding any expense, we check if the user is FREE or PREMIUM.
// - FREE users: Max 7 transactions per day (expense + income combined on a per-service basis)
// - PREMIUM users: Unlimited transactions
//
// This Feign call is made BEFORE saving the expense in addExpense().
// If the user is FREE and has hit 7 expenses today → 403 Forbidden.
// ============================================================================
@FeignClient(name = "auth-service")
public interface AuthClient {

	// Gets the user's subscription: { "subscriptionType": "FREE", "premiumExpiresAt": null }
	@GetMapping("/auth/subscription/{userId}")
	Map<String, Object> getSubscriptionStatus(@PathVariable("userId") int userId);
}
