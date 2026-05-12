package com.spendsmart.budget.resource;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;
import com.spendsmart.budget.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetResource {

	private final BudgetService budgetService;
	@Operation(summary = "Create a new budget", description = "Creates a new budget for a specific user and category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
	@PostMapping
	public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
		return new ResponseEntity<>(budgetService.createBudget(budget), HttpStatus.CREATED);
	}

	// ── Admin Read-Only Endpoints ──────────────────────────────────────
	@GetMapping("/admin")
	public ResponseEntity<List<Budget>> getAllBudgets() {
		return ResponseEntity.ok(budgetService.getAllBudgets());
	}

	@GetMapping("/admin/{budgetId}")
	public ResponseEntity<Budget> getBudgetByIdAdmin(@PathVariable Integer budgetId) {
		return budgetService.getBudgetById(budgetId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{budgetId}")
	public ResponseEntity<Budget> getById(@PathVariable Integer budgetId) {
		return budgetService.getBudgetById(budgetId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Budget>> getByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(budgetService.getBudgetsByUser(userId));
	}

	@GetMapping("/user/{userId}/active")
	public ResponseEntity<List<Budget>> getActive(@PathVariable Integer userId) {
		return ResponseEntity.ok(budgetService.getActiveBudgets(userId));
	}

	@PutMapping("/{budgetId}")
	public ResponseEntity<Budget> update(@PathVariable Integer budgetId, @RequestBody Budget budget) {
		return ResponseEntity.ok(budgetService.updateBudget(budgetId, budget));
	}

	@DeleteMapping("/{budgetId}")
	public ResponseEntity<Void> delete(@PathVariable Integer budgetId) {
		budgetService.deleteBudget(budgetId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{budgetId}/spent")
	public ResponseEntity<Void> updateSpentAmount(@PathVariable Integer budgetId, @RequestParam BigDecimal amount) {
		budgetService.updateSpentAmount(budgetId, amount);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{budgetId}/progress")
	public ResponseEntity<BudgetProgress> getProgress(@PathVariable Integer budgetId) {
		return ResponseEntity.ok(budgetService.getBudgetProgress(budgetId));
	}

	@GetMapping("/user/{userId}/alerts")
	public ResponseEntity<List<String>> getAlerts(@PathVariable Integer userId) {
		return ResponseEntity.ok(budgetService.checkBudgetAlerts(userId));
	}

	@PostMapping("/user/{userId}/reset")
	public ResponseEntity<Void> resetPeriod(@PathVariable Integer userId) {
		budgetService.resetBudgetPeriod(userId);
		return ResponseEntity.ok().build();
	}

	// OpenFeign integration
	@PutMapping("/user/{userId}/category/{categoryId}/spent")
	public ResponseEntity<Void> updateSpentAmountByCategory(@PathVariable Integer userId,
			@PathVariable Integer categoryId, @RequestParam BigDecimal amount) {

		// Find the budget for this user and category, and update it if it exists
		budgetService.getBudgetsByCategory(userId, categoryId).ifPresent(budget -> {
			budgetService.updateSpentAmount(budget.getBudgetId(), amount);
		});

		return ResponseEntity.ok().build();
	}

	// for analytics service
	@GetMapping("/user/{userId}/adherence")
	public ResponseEntity<Double> getOverallBudgetAdherence(@PathVariable Integer userId) {
		return ResponseEntity.ok(budgetService.getOverallBudgetAdherence(userId));
	}
}