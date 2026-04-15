package com.spendsmart.web.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "auth-client", url = "http://localhost:8080/users") // Or /auth depending on your gateway routing
public interface AuthClient {

	@GetMapping("/count")
	long getTotalUserCount();

	@GetMapping
	List<Object> getAllUsers(); // Ideally replace Object with a UserDto later!

	@PostMapping("/{id}/suspend")
	void suspendUser(@PathVariable("id") int id);

	@DeleteMapping("/{id}")
	void deleteUser(@PathVariable("id") int id);

	@GetMapping("/active-ids")
	List<Integer> getAllActiveUserIds();
}