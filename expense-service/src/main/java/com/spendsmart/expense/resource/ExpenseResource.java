package com.spendsmart.expense.resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.service.ExpenseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseResource {
	private final ExpenseService expenseService;

	@PostMapping
	public ResponseEntity<Expense> addExpense(@Valid @RequestBody Expense expense) {
		return new ResponseEntity<>(expenseService.addExpense(expense), HttpStatus.CREATED);
	}

	// ── Admin Read-Only Endpoints ──────────────────────────────────────
	@GetMapping("/admin")
	public ResponseEntity<List<Expense>> getAllExpenses() {
		return ResponseEntity.ok(expenseService.getAllExpenses());
	}

	@GetMapping("/admin/{expenseId}")
	public ResponseEntity<Expense> getExpenseByIdAdmin(@PathVariable Long expenseId) {
		return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
	}

	@GetMapping("/{expenseId}")
	public ResponseEntity<Expense> getExpenseById(@PathVariable Long expenseId) {
		return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Expense>> getExpensesByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable Integer categoryId) {
		return ResponseEntity.ok(expenseService.getExpensesByCategory(categoryId));
	}

	@GetMapping("/user/{userId}/dateRange")
	public ResponseEntity<List<Expense>> getExpensesByDateRange(@PathVariable Integer userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return ResponseEntity.ok(expenseService.getExpensesByDateRange(userId, startDate, endDate));
	}

	@GetMapping("/user/{userId}/type")
	public ResponseEntity<List<Expense>> getExpensesByType(@PathVariable Integer userId,
			@RequestParam ExpenseType type) {
		return ResponseEntity.ok(expenseService.getExpensesByType(userId, type));
	}

	@GetMapping("/user/{userId}/search")
	public ResponseEntity<List<Expense>> searchExpenses(@PathVariable Integer userId, @RequestParam String keyword) {
		return ResponseEntity.ok(expenseService.searchExpenses(userId, keyword));
	}

	@PutMapping("/{expenseId}")
	public ResponseEntity<Expense> updateExpense(@PathVariable Long expenseId, @Valid @RequestBody Expense expense) {
		return ResponseEntity.ok(expenseService.updateExpense(expenseId, expense));
	}

	@DeleteMapping("/{expenseId}")
	public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
		expenseService.deleteExpense(expenseId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/user/{userId}/total")
	public ResponseEntity<BigDecimal> getTotalByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(expenseService.getTotalByUser(userId));
	}

	@GetMapping("/category/{categoryId}/total")
	public ResponseEntity<BigDecimal> getTotalByCategory(@PathVariable Integer categoryId) {
		return ResponseEntity.ok(expenseService.getTotalByCategory(categoryId));
	}

	// for analytics service
	@GetMapping("/user/{userId}/year/total")
	public ResponseEntity<BigDecimal> getTotalExpenseByYear(@PathVariable Integer userId, @RequestParam int year) {
		return ResponseEntity.ok(expenseService.getTotalExpenseByYear(userId, year));
	}

	@GetMapping("/user/{userId}/month/breakdown")
	public ResponseEntity<Map<String, BigDecimal>> getExpenseBreakdownByCategory(@PathVariable Integer userId,
			@RequestParam int year, @RequestParam int month) {
		return ResponseEntity.ok(expenseService.getExpenseBreakdownByCategory(userId, year, month));
	}

	@GetMapping("/user/{userId}/month/daily")
	public ResponseEntity<Map<Integer, BigDecimal>> getDailyExpenseTrend(@PathVariable Integer userId,
			@RequestParam int year, @RequestParam int month) {
		return ResponseEntity.ok(expenseService.getDailyExpenseTrend(userId, year, month));
	}
	
	@GetMapping("/user/{userId}/month/total")
    public ResponseEntity<BigDecimal> getTotalExpenseByMonth(
            @PathVariable Integer userId, 
            @RequestParam int year, 
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getTotalExpenseByMonth(userId, year, month));
    }
}
