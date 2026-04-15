package com.spendsmart.auth.model.enums;

// ============================================================================
// SUBSCRIPTION TYPE — Controls the freemium access model.
//
// FREE users:
//   - Can make up to 7 transactions per day (combined expense + income)
//   - After 7, they get HTTP 403 with "Daily limit reached. Upgrade to Premium."
//
// PREMIUM users:
//   - Unlimited transactions per day
//   - Subscription lasts 30 days from the payment date
//   - After expiry, user automatically reverts to FREE
//
// The subscription is checked by expense-service and income-service
// before every addExpense() and addIncome() call via a Feign call to auth-service.
// ============================================================================
public enum SubscriptionType {
	FREE,
	PREMIUM
}
