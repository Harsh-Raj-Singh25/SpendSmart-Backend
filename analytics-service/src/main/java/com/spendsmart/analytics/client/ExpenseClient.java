package com.spendsmart.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.spendsmart.analytics.model.dto.ExpenseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "expense-service", url = "http://localhost:8082")
public interface ExpenseClient {
	@GetMapping("/expenses/user/{userId}/month/total")
	BigDecimal getTotalExpenseByMonth(@PathVariable("userId") Integer userId, @RequestParam("year") int year,
			@RequestParam("month") int month);

	@GetMapping("/expenses/user/{userId}/month/breakdown")
	Map<String, BigDecimal> getExpenseBreakdownByCategory(@PathVariable("userId") Integer userId,
			@RequestParam("year") int year, @RequestParam("month") int month);

	@GetMapping("/expenses/user/{userId}/year/total")
	BigDecimal getTotalExpenseByYear(@PathVariable("userId") Integer userId, @RequestParam("year") int year);

	@GetMapping("/expenses/user/{userId}/month/daily")
	Map<Integer, BigDecimal> getDailyExpenseTrend(@PathVariable("userId") Integer userId,
			@RequestParam("year") int year, @RequestParam("month") int month);

	@GetMapping("/user/{userId}")
	List<ExpenseDto> getUserExpenses(@PathVariable("userId") int userId);
}