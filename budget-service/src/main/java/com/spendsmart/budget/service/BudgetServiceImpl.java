package com.spendsmart.budget.service;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.model.dto.BudgetProgress;
import com.spendsmart.budget.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//@Slf4j
@Transactional
public class BudgetServiceImpl implements BudgetService {

	private final BudgetRepository budgetRepository;
	Logger log=new Logger();
	@Override
	public Budget createBudget(Budget budget) {
		log.info("Creating budget for user: {} and category: {}", budget.getUserId(), budget.getCategoryId());
		// Check if active budget already exists for this category
		Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryId(budget.getUserId(),
				budget.getCategoryId());
		if (existing.isPresent() && existing.get().getIsActive()) {
			throw new RuntimeException("An active budget already exists for this category.");
		}
		return budgetRepository.save(budget);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Budget> getBudgetById(Integer budgetId) {
		return budgetRepository.findByBudgetId(budgetId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Budget> getBudgetsByUser(Integer userId) {
		return budgetRepository.findByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Budget> getActiveBudgets(Integer userId) {
		return budgetRepository.findByUserIdAndIsActive(userId, true);
	}

	@Override
	public Budget updateBudget(Integer budgetId, Budget budgetDetails) {
		Budget budget = budgetRepository.findByBudgetId(budgetId)
				.orElseThrow(() -> new RuntimeException("Budget not found"));

		budget.setName(budgetDetails.getName());
		budget.setLimitAmount(budgetDetails.getLimitAmount());
		budget.setPeriod(budgetDetails.getPeriod());
		budget.setStartDate(budgetDetails.getStartDate());
		budget.setEndDate(budgetDetails.getEndDate());
		budget.setAlertThreshold(budgetDetails.getAlertThreshold());
		budget.setIsActive(budgetDetails.getIsActive());

		return budgetRepository.save(budget);
	}

	@Override
	public void deleteBudget(Integer budgetId) {
		budgetRepository.deleteByBudgetId(budgetId);
	}

	@Override
	public void updateSpentAmount(Integer budgetId, BigDecimal expenseAmount) {
		Budget budget = budgetRepository.findByBudgetId(budgetId)
				.orElseThrow(() -> new RuntimeException("Budget not found"));

		// Add the new expense amount to the existing spent amount
		BigDecimal newTotal = budget.getSpentAmount().add(expenseAmount);
		// Guard against negative values (can happen if budget service was down during add)
		if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
			newTotal = BigDecimal.ZERO;
		}
		budget.setSpentAmount(newTotal);
		budgetRepository.save(budget);

		log.info("Updated spent amount for budget {}. New Total: {}", budgetId, newTotal);
	}

	@Override
	@Transactional(readOnly = true)
	public BudgetProgress getBudgetProgress(Integer budgetId) {
		Budget budget = budgetRepository.findByBudgetId(budgetId)
				.orElseThrow(() -> new RuntimeException("Budget not found"));

		BigDecimal limit = budget.getLimitAmount();
		BigDecimal spent = budget.getSpentAmount();
		BigDecimal remaining = limit.subtract(spent);

		// Calculate percentage: (spent / limit) * 100
		BigDecimal percentage = BigDecimal.ZERO;
		if (limit.compareTo(BigDecimal.ZERO) > 0) {
			percentage = spent.divide(limit, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2,
					RoundingMode.HALF_UP);
		}

		// Determine Alert Status
		String status = "SAFE";
		if (percentage.compareTo(new BigDecimal("100")) >= 0) {
			status = "EXCEEDED";
		} else if (percentage.compareTo(new BigDecimal(budget.getAlertThreshold())) >= 0) {
			status = "WARNING";
		}

		return BudgetProgress.builder().budgetId(budget.getBudgetId()).limitAmount(limit).spentAmount(spent)
				.remainingAmount(remaining).percentageUsed(percentage).alertStatus(status).build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> checkBudgetAlerts(Integer userId) {
		List<Budget> activeBudgets = getActiveBudgets(userId);
		List<String> alerts = new ArrayList<>();

		for (Budget budget : activeBudgets) {
			BudgetProgress progress = getBudgetProgress(budget.getBudgetId());
			if ("EXCEEDED".equals(progress.getAlertStatus())) {
				alerts.add("CRITICAL: You have exceeded your budget for " + budget.getName() + "!");
			} else if ("WARNING".equals(progress.getAlertStatus())) {
				alerts.add("WARNING: You have used " + progress.getPercentageUsed() + "% of your " + budget.getName()
						+ " budget.");
			}
		}
		return alerts;
	}

	@Override
	public void resetBudgetPeriod(Integer userId) {
		log.info("Resetting active budgets for user: {}", userId);
		List<Budget> activeBudgets = getActiveBudgets(userId);

		for (Budget budget : activeBudgets) {
			budget.setSpentAmount(BigDecimal.ZERO);
			// In a real system, you would also advance the startDate and endDate here based
			// on the Period
			budgetRepository.save(budget);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Budget> getBudgetsByCategory(Integer userId, Integer categoryId) {
		return budgetRepository.findByUserIdAndCategoryId(userId, categoryId);
	}

	// for analytics service
	@Override
	@Transactional(readOnly = true)
	public Double getOverallBudgetAdherence(Integer userId) {
		
		List<Budget> activeBudgets = budgetRepository.findByUserIdAndIsActive(userId, true);
		if (activeBudgets.isEmpty())
			return 0.0;

		double totalPercentage = 0.0;
		for (Budget budget : activeBudgets) {
			if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
				double percentage = budget.getSpentAmount().divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
						.doubleValue() * 100;
				// Cap adherence at 100% so massive overspending doesn't mathematically break
				// the average
				totalPercentage += Math.min(percentage, 100.0);
			}
		}

		// Return the average adherence across all active budgets
		return totalPercentage / activeBudgets.size();
	}
}