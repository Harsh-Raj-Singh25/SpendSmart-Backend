package com.spendsmart.recurring.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.spendsmart.recurring.model.dto.TransactionRequest;
@FeignClient(name="expense-service")
public interface ExpenseClient {
	
	@PostMapping("/expenses")
	void addExpense(@RequestBody TransactionRequest request);
}
