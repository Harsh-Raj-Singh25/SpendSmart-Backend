package com.spendsmart.recurring.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.spendsmart.recurring.model.dto.TransactionRequest;

@FeignClient(name = "income-service", url = "http://localhost:8083")
public interface IncomeClient {
	@PostMapping("/incomes")
	void addIncome(@RequestBody TransactionRequest request);
}