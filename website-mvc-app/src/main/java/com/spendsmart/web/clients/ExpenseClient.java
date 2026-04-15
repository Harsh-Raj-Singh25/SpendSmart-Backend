package com.spendsmart.web.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.spendsmart.web.dto.TransactionDto;

@FeignClient(name = "expense-client", url = "http://localhost:8080/expenses")
public interface ExpenseClient {
	@GetMapping("/user/{userId}")
	List<TransactionDto> getUserExpenses(@PathVariable("userId") int userId);

	@PostMapping
	TransactionDto addExpense(@RequestBody TransactionDto expense);

	@DeleteMapping("/{id}")
	void deleteExpense(@PathVariable("id") int id);
}