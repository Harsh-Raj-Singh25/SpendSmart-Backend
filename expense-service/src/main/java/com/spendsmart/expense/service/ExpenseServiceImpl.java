package com.spendsmart.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spendsmart.event.ExpenseCreatedEvent;
import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.config.RabbitMQConfig;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.model.enums.ExpenseType;
import com.spendsmart.expense.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseServiceImpl implements ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final BudgetServiceClient budgetServiceClient;
	// RabbitMQ implementation
	private final RabbitTemplate rabbitTemplate;

	// Feign client to check user's subscription status (FREE vs PREMIUM)
	private final com.spendsmart.expense.client.AuthClient authClient;

	// Maximum number of free transactions per day for FREE-tier users
	private static final int FREE_DAILY_LIMIT = 7;

	@Override
	public Expense addExpense(Expense expense) {
		log.info("Adding new expense for user: {}", expense.getUserId());

		// ── FREEMIUM LIMIT CHECK ────────────────────────────────────────
		// Before saving, check if the user is on the FREE tier.
		// FREE users can only add 7 expenses per day.
		// PREMIUM users have unlimited expenses.
		try {
			var subscriptionStatus = authClient.getSubscriptionStatus(expense.getUserId());
			String subscriptionType = (String) subscriptionStatus.get("subscriptionType");

			if ("FREE".equals(subscriptionType)) {
				// Count how many expenses this user already has today
				long todayCount = expenseRepository.countByUserIdAndDate(expense.getUserId(), LocalDate.now());

				if (todayCount >= FREE_DAILY_LIMIT) {
					throw new RuntimeException("Daily limit reached! FREE users can add up to " + FREE_DAILY_LIMIT
							+ " expenses per day. " + "Upgrade to Premium for unlimited access.");
				}
			}
		} catch (RuntimeException e) {
			throw e; // Re-throw our limit exceeded exception
		} catch (Exception e) {
			// If auth-service is down, allow the expense (graceful degradation)
			log.warn("Could not check subscription status. Allowing expense: {}", e.getMessage());
		}

		Expense savedExpense = expenseRepository.save(expense);

		// ── SYNC WITH BUDGET SERVICE (Synchronous) ──────────────────────
		try {
			budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), savedExpense.getCategoryId(),
					savedExpense.getAmount());
			log.info("Successfully updated budget for category: {}", savedExpense.getCategoryId());
		} catch (Exception e) {
			log.error("Failed to update budget. Budget Service might be down: {}", e.getMessage());
		}

		// ── FIRE EVENT TO RABBITMQ (Asynchronous) ───────────────────────
		try {
			ExpenseCreatedEvent event = new ExpenseCreatedEvent(savedExpense.getUserId(), savedExpense.getTitle(),
					savedExpense.getAmount());

			// Fire and forget: send to the exchange
			rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
			log.info("Successfully published ExpenseCreatedEvent for user: {}", savedExpense.getUserId());

		} catch (Exception e) {
			// If RabbitMQ is down, we don't crash the request!
			// The expense is already saved safely in the database.
			log.error("Failed to publish to RabbitMQ. Notifications may be delayed: {}", e.getMessage());
		}

		return savedExpense;
	}

	@Override
	@Transactional(readOnly = true)
	public Expense getExpenseById(Long expenseId) { 
		return expenseRepository.findByExpenseId(expenseId)
				.orElseThrow(() -> new RuntimeException("Expense not found with ID: " + expenseId));

	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByUser(Integer userId) { 
		return expenseRepository.findByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByCategory(Integer categoryId) { 
		return expenseRepository.findByCategoryId(categoryId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByDateRange(Integer userId, LocalDate start, LocalDate end) { 
		return expenseRepository.findByUserIdAndDateBetween(userId, start, end);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByMonth(Integer userId, int year, int month) {
		// TODO Auto-generated method stub
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();
		return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Expense> getExpensesByType(Integer userId, ExpenseType type) {
		// TODO Auto-generated method stub
		return expenseRepository.findByUserIdAndType(userId, type);
	}

	@Override
	public List<Expense> searchExpenses(Integer userId, String keyword) { 
		return expenseRepository.searchExpensesByKeyword(userId, keyword);
	}

	@Override
	public Expense updateExpense(Long expenseId, Expense expenseDetails) { 
		log.info("Updating expense ID:{}", expenseId);
		Expense existingExpense = getExpenseById(expenseId);

		// Store the old values BEFORE we change them so we can do math later
		BigDecimal oldAmount = existingExpense.getAmount();
		Integer oldCategoryId = existingExpense.getCategoryId();

		existingExpense.setCategoryId(expenseDetails.getCategoryId());
		existingExpense.setTitle(expenseDetails.getTitle());
		existingExpense.setAmount(expenseDetails.getAmount());
		existingExpense.setCurrency(expenseDetails.getCurrency());
		existingExpense.setType(expenseDetails.getType());
		existingExpense.setPaymentMethod(expenseDetails.getPaymentMethod());
		existingExpense.setDate(expenseDetails.getDate());
		existingExpense.setNotes(expenseDetails.getNotes());
		existingExpense.setReceiptUrl(expenseDetails.getReceiptUrl());
		existingExpense.setIsRecurring(expenseDetails.getIsRecurring());

		Expense savedExpense = expenseRepository.save(existingExpense); 
		// Sync with Budget Service
		try {
			if (!oldCategoryId.equals(savedExpense.getCategoryId())) {
				// Scenario A: The user changed the category of the expense!
				// 1. Refund the old budget
				budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), oldCategoryId,
						oldAmount.negate());
				// 2. Charge the new budget
				budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), savedExpense.getCategoryId(),
						savedExpense.getAmount());
				log.info("Cross-category budget sync completed.");
			} else if (oldAmount.compareTo(savedExpense.getAmount()) != 0) {
				// Scenario B: The category is the same, but the amount changed
				BigDecimal difference = savedExpense.getAmount().subtract(oldAmount);
				budgetServiceClient.updateSpentAmountByCategory(savedExpense.getUserId(), savedExpense.getCategoryId(),
						difference);
				log.info("Budget sync completed. Amount adjusted by: {}", difference);
			}
		} catch (Exception e) {
			log.error("Failed to sync budget during expense update. Budget service might be down: {}", e.getMessage());
		}

		return savedExpense;
	}

	@Override
	public void deleteExpense(Long expenseId) {
		log.info("Deleting expense ID: {}", expenseId);
		Expense existingExpense = getExpenseById(expenseId);

		expenseRepository.deleteByExpenseId(expenseId);

		// Sync with Budget Service by sending a negative amount (Refund)
		try {
			budgetServiceClient.updateSpentAmountByCategory(existingExpense.getUserId(),
					existingExpense.getCategoryId(), existingExpense.getAmount().negate() // .negate() turns 500 into
																							// -500
			);
			log.info("Successfully refunded budget for deleted expense.");
		} catch (Exception e) {
			log.error("Failed to refund budget. Budget service might be down: {}", e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalByUser(Integer userId) { 
		BigDecimal total = expenseRepository.sumAmountByUserId(userId);
		return total != null ? total : BigDecimal.ZERO;
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalByCategory(Integer categoryId) { 
		BigDecimal total = expenseRepository.sumAmountByCategoryId(categoryId);
		return total != null ? total : BigDecimal.ZERO;
	}

	// for analytics service
	@Override
	public BigDecimal getTotalExpenseByYear(Integer userId, int year) {
		LocalDate start = LocalDate.of(year, 1, 1);
		LocalDate end = LocalDate.of(year, 12, 31);
		BigDecimal total = expenseRepository.sumAmountByUserIdAndDateBetween(userId, start, end);
		return total != null ? total : BigDecimal.ZERO;
	}

	@Override
	public Map<String, BigDecimal> getExpenseBreakdownByCategory(Integer userId, int year, int month) {
		LocalDate start = YearMonth.of(year, month).atDay(1);
		LocalDate end = YearMonth.of(year, month).atEndOfMonth();
		List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

		// Grouping by Category ID (Returned as a String to match the Feign contract)
		return expenses.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getCategoryId()), // e.g., "101"
				Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
	}

	@Override
	public Map<Integer, BigDecimal> getDailyExpenseTrend(Integer userId, int year, int month) {
		LocalDate start = YearMonth.of(year, month).atDay(1);
		LocalDate end = YearMonth.of(year, month).atEndOfMonth();
		List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

		// Grouping by the Day of the Month
		return expenses.stream().collect(Collectors.groupingBy(e -> e.getDate().getDayOfMonth(),
				Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
	}

	@Override
	public BigDecimal getTotalExpenseByMonth(Integer userId, int year, int month) {
		LocalDate start = java.time.YearMonth.of(year, month).atDay(1);
		LocalDate end = java.time.YearMonth.of(year, month).atEndOfMonth();
		BigDecimal total = expenseRepository.sumAmountByUserIdAndDateBetween(userId, start, end);
		return total != null ? total : BigDecimal.ZERO;
	}
	
	

}
