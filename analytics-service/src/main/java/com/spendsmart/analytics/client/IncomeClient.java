package com.spendsmart.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.spendsmart.analytics.model.dto.IncomeDto;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "income-service")
public interface IncomeClient {
	@GetMapping("/incomes/user/{userId}/month/total")
	BigDecimal getTotalIncomeByMonth(@PathVariable("userId") Integer userId, @RequestParam("year") int year,
			@RequestParam("month") int month);

	@GetMapping("/incomes/user/{userId}/year/total")
	BigDecimal getTotalIncomeByYear(@PathVariable("userId") Integer userId, @RequestParam("year") int year);

	@GetMapping("/incomes/user/{userId}")
	List<IncomeDto> getUserIncomes(@PathVariable("userId") int userId);
}