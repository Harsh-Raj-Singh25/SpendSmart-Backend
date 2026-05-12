package com.spendsmart.recurring.resource;

import com.spendsmart.recurring.entity.RecurringTransaction;
import com.spendsmart.recurring.service.RecurringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring")
@RequiredArgsConstructor
public class RecurringResource {

	private final RecurringService recurringService;

	@PostMapping
	public ResponseEntity<RecurringTransaction> add(@RequestBody RecurringTransaction transaction) {
		return new ResponseEntity<>(recurringService.addRecurring(transaction), HttpStatus.CREATED);
	}

	// ── Admin Read-Only Endpoints ──────────────────────────────────────
	@GetMapping("/admin")
	public ResponseEntity<List<RecurringTransaction>> getAllRecurring() {
		return ResponseEntity.ok(recurringService.getAllRecurring());
	}

	@GetMapping("/admin/{id}")
	public ResponseEntity<RecurringTransaction> getByIdAdmin(@PathVariable Integer id) {
		return recurringService.getById(id).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<RecurringTransaction>> getByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(recurringService.getByUser(userId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RecurringTransaction> getById(@PathVariable Integer id) {
		return recurringService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}/active")
	public ResponseEntity<List<RecurringTransaction>> getActive(@PathVariable Integer userId) {
		return ResponseEntity.ok(recurringService.getActiveRecurring(userId));
	}

	@GetMapping("/user/{userId}/upcoming")
	public ResponseEntity<List<RecurringTransaction>> getUpcomingThisMonth(@PathVariable Integer userId) {
		return ResponseEntity.ok(recurringService.getUpcomingThisMonth(userId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<RecurringTransaction> update(@PathVariable Integer id,
			@RequestBody RecurringTransaction transaction) {
		return ResponseEntity.ok(recurringService.updateRecurring(id, transaction));
	}

	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
		recurringService.deactivateRecurring(id);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		recurringService.deleteRecurring(id);
		return ResponseEntity.noContent().build();
	}
}