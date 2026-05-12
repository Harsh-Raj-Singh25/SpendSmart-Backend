package com.spendsmart.income.resource;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.model.enums.IncomeSource;
import com.spendsmart.income.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeResource {

	private final IncomeService incomeService;

	@PostMapping
	public ResponseEntity<Income> addIncome(@RequestBody Income income) {
		return new ResponseEntity<>(incomeService.addIncome(income), HttpStatus.CREATED);
	}

	// ── Admin Read-Only Endpoints ──────────────────────────────────────
	@GetMapping("/admin")
	public ResponseEntity<List<Income>> getAllIncomes() {
		return ResponseEntity.ok(incomeService.getAllIncomes());
	}

	@GetMapping("/admin/{incomeId}")
	public ResponseEntity<Income> getIncomeByIdAdmin(@PathVariable Integer incomeId) {
		return ResponseEntity.ok(incomeService.getIncomeById(incomeId));
	}

	@GetMapping("/{incomeId}")
	public ResponseEntity<Income> getIncomeById(@PathVariable Integer incomeId) {
		return ResponseEntity.ok(incomeService.getIncomeById(incomeId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Income>> getIncomesByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(incomeService.getIncomesByUser(userId));
	}

	@GetMapping("/user/{userId}/source")
	public ResponseEntity<List<Income>> getIncomesBySource(@PathVariable Integer userId,
			@RequestParam IncomeSource source) {
		return ResponseEntity.ok(incomeService.getIncomesBySource(userId, source));
	}

	@GetMapping("/user/{userId}/dateRange")
	public ResponseEntity<List<Income>> getIncomesByDateRange(@PathVariable Integer userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return ResponseEntity.ok(incomeService.getIncomesByDateRange(userId, startDate, endDate));
	}

	@GetMapping("/user/{userId}/month")
	public ResponseEntity<List<Income>> getIncomesByMonth(@PathVariable Integer userId, @RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(incomeService.getIncomesByMonth(userId, year, month));
	}

	@GetMapping("/user/{userId}/recurring")
	public ResponseEntity<List<Income>> getRecurringIncomes(@PathVariable Integer userId) {
		return ResponseEntity.ok(incomeService.getRecurringIncomes(userId));
	}

	@PutMapping("/{incomeId}")
	public ResponseEntity<Income> updateIncome(@PathVariable Integer incomeId, @RequestBody Income income) {
		return ResponseEntity.ok(incomeService.updateIncome(incomeId, income));
	}

	@DeleteMapping("/{incomeId}")
	public ResponseEntity<Void> deleteIncome(@PathVariable Integer incomeId) {
		incomeService.deleteIncome(incomeId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/user/{userId}/total")
	public ResponseEntity<BigDecimal> getTotalIncomeByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(incomeService.getTotalIncomeByUser(userId));
	}

	@GetMapping("/user/{userId}/month/total")
	public ResponseEntity<BigDecimal> getTotalIncomeByMonth(@PathVariable Integer userId, @RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(incomeService.getTotalIncomeByMonth(userId, year, month));
	}

	// for analytics service
	@GetMapping("/user/{userId}/year/total")
	public ResponseEntity<BigDecimal> getTotalIncomeByYear(@PathVariable Integer userId, @RequestParam int year) {
		return ResponseEntity.ok(incomeService.getTotalIncomeByYear(userId, year));
	}
}