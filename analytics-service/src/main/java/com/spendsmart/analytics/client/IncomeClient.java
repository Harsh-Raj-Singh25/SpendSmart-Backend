package com.spendsmart.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "income-service", url = "http://localhost:8083")
public interface IncomeClient {
	@GetMapping("/incomes/user/{userId}/month/total")
	BigDecimal getTotalIncomeByMonth(@PathVariable("userId") Integer userId, @RequestParam("year") int year,
			@RequestParam("month") int month);

	@GetMapping("/incomes/user/{userId}/year/total")
	BigDecimal getTotalIncomeByYear(@PathVariable("userId") Integer userId, @RequestParam("year") int year);
}