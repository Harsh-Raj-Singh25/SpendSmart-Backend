package com.spendsmart.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "budget-service")
public interface BudgetClient {
	// Assuming you have an endpoint that returns the overall adherence percentage
	// (e.g., 85.5)
	@GetMapping("/budgets/user/{userId}/adherence")
	Double getOverallBudgetAdherence(@PathVariable("userId") Integer userId);
}