package com.spendsmart.budget.service;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetService {
	Budget createBudget(Budget budget);

	Optional<Budget> getBudgetById(Integer budgetId);

	List<Budget> getBudgetsByUser(Integer userId);

	List<Budget> getActiveBudgets(Integer userId);

	Budget updateBudget(Integer budgetId, Budget budgetDetails);

	void deleteBudget(Integer budgetId);

	// Core Business Logic Operations
	void updateSpentAmount(Integer budgetId, BigDecimal expenseAmount);

	BudgetProgress getBudgetProgress(Integer budgetId);

	List<String> checkBudgetAlerts(Integer userId);

	void resetBudgetPeriod(Integer userId);

	Optional<Budget> getBudgetsByCategory(Integer userId, Integer categoryId);
	
	// for analytics service
	Double getOverallBudgetAdherence(Integer userId);
}