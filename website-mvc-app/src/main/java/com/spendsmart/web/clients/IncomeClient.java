package com.spendsmart.web.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.spendsmart.web.dto.TransactionDto;

@FeignClient(name = "income-client", url = "http://localhost:8080/incomes")
public interface IncomeClient {
	@GetMapping("/user/{userId}")
	List<TransactionDto> getUserIncomes(@PathVariable("userId") int userId);

	@PostMapping
	TransactionDto addIncome(@RequestBody TransactionDto income);
}