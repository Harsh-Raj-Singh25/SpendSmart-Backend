package com.spendsmart.budget.resource;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;
import com.spendsmart.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetResource {

	private final BudgetService budgetService;

	@PostMapping
	public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
		return new ResponseEntity<>(budgetService.createBudget(budget), HttpStatus.CREATED);
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
    public ResponseEntity<Void> updateSpentAmountByCategory(
            @PathVariable Integer userId,
            @PathVariable Integer categoryId,
            @RequestParam BigDecimal amount) {
        
        // Find the budget for this user and category, and update it if it exists
        budgetService.getBudgetsByCategory(userId, categoryId).ifPresent(budget -> {
            budgetService.updateSpentAmount(budget.getBudgetId(), amount);
        });
        
        return ResponseEntity.ok().build();
    }
}