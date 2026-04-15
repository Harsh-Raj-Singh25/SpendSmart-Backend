package com.spendsmart.web.clients;

import com.spendsmart.web.dto.RecurringDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "recurring-client", url = "http://localhost:8080/recurring")
public interface RecurringClient {

	@GetMapping("/user/{userId}")
	List<RecurringDto> getUserRecurringTransactions(@PathVariable("userId") int userId);

	@GetMapping("/user/{userId}/active")
	List<RecurringDto> getActiveRecurringTransactions(@PathVariable("userId") int userId);

	@PostMapping
	RecurringDto addRecurringTransaction(@RequestBody RecurringDto recurringDto);

	@PatchMapping("/{id}/deactivate")
	void deactivateRecurringTransaction(@PathVariable("id") int id);

	@DeleteMapping("/{id}")
	void deleteRecurringTransaction(@PathVariable("id") int id);
}